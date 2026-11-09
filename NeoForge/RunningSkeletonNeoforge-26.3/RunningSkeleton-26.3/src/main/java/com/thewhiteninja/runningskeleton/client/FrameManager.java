package com.thewhiteninja.runningskeleton.client;

import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.renderpearl.api.textures.FilterMode;
import com.thewhiteninja.runningskeleton.RunningSkeletonMod;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.Util;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.util.ArrayDeque;
import java.util.Locale;
import java.util.Optional;
import java.util.concurrent.CancellationException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;

/**
 * Decodes the animation frames on background threads and uploads them to the GPU on the render
 * thread, a few per tick.
 *
 * <p>Only a sliding window of frames around the playback position is kept in flight, the native
 * image of every frame is closed right after its upload, and frames that have already been passed
 * are released. All methods must be called from the render thread.</p>
 */
public final class FrameManager {

    /** Total number of frames of the animation. */
    public static final int FRAME_COUNT = 79;

    /** Maximum number of frames that are decoding or waiting for upload at the same time. */
    public static final int MAX_PREFETCH = 12;

    /** Number of contiguous uploaded frames required before playback starts. */
    public static final int START_BUFFER = 8;

    /** Maximum number of frames uploaded to the GPU per call to {@link #update(int)}. */
    public static final int MAX_UPLOADS_PER_TICK = 3;

    private static final String ASSET_DIRECTORY = "textures/animation/";

    private static final Identifier[] SOURCE_IDS = new Identifier[FRAME_COUNT];
    private static final Identifier[] TEXTURE_IDS = new Identifier[FRAME_COUNT];

    static {
        for (int i = 0; i < FRAME_COUNT; i++) {
            SOURCE_IDS[i] = Identifier.fromNamespaceAndPath(
                    RunningSkeletonMod.MOD_ID, ASSET_DIRECTORY + fileName(i));
            TEXTURE_IDS[i] = Identifier.fromNamespaceAndPath(
                    RunningSkeletonMod.MOD_ID, String.format(Locale.ROOT, "dynamic/frame_%04d", i + 1));
        }
    }

    private final boolean[] resident = new boolean[FRAME_COUNT];
    private final int[] widths = new int[FRAME_COUNT];
    private final int[] heights = new int[FRAME_COUNT];
    private final ArrayDeque<PendingFrame> pending = new ArrayDeque<>();

    private ResourceManager resourceManager;
    private int nextToDecode;
    private int contiguousReady;
    private boolean failed;

    /**
     * Returns the file name of a frame.
     *
     * @param index zero-based frame index
     * @return the file name, for example {@code frame_0001.png}
     */
    public static String fileName(int index) {
        return String.format(Locale.ROOT, "frame_%04d.png", index + 1);
    }

    /**
     * Starts loading from the first frame, discarding any previous state.
     */
    public void start() {
        release();
        resourceManager = Minecraft.getInstance().getResourceManager();
        failed = false;
        scheduleDecodes(0);
    }

    /**
     * Advances loading: uploads ready frames, schedules new decodes and frees frames already passed.
     *
     * @param baseIndex the index of the frame currently being played (0 while loading)
     */
    public void update(int baseIndex) {
        if (failed) {
            return;
        }
        uploadReady(baseIndex);
        if (failed) {
            return;
        }
        scheduleDecodes(baseIndex);
        releasePassedFrames(baseIndex);
    }

    /**
     * Returns whether a frame failed to load. The failure has already been logged and reported.
     *
     * @return true if loading failed and the animation must be cancelled
     */
    public boolean hasFailed() {
        return failed;
    }

    /**
     * Returns how many frames, counted from the first one, are uploaded without gaps.
     *
     * @return the number of contiguous frames ready from the start
     */
    public int contiguousReady() {
        return contiguousReady;
    }

    /**
     * Returns the frame to draw for a playback position: the frame itself if it is uploaded,
     * otherwise the most recent earlier frame that is.
     *
     * @param index zero-based playback frame index
     * @return the texture and its pixel size, or null if no frame is available
     */
    public FrameView frameFor(int index) {
        int display = displayIndex(index);
        return display < 0 ? null : new FrameView(TEXTURE_IDS[display], widths[display], heights[display]);
    }

    /**
     * Drops every pending decode job and frees every GPU texture.
     *
     * <p>The futures are not cancelled; instead, every image they produce is closed as soon as it
     * completes, so no native memory is leaked.</p>
     */
    public void release() {
        for (PendingFrame frame : pending) {
            discard(frame.future());
        }
        pending.clear();
        for (int i = 0; i < FRAME_COUNT; i++) {
            if (resident[i]) {
                free(i);
            }
        }
        nextToDecode = 0;
        contiguousReady = 0;
        resourceManager = null;
    }

    private void scheduleDecodes(int baseIndex) {
        if (nextToDecode < baseIndex) {
            nextToDecode = baseIndex;
        }
        ResourceManager manager = resourceManager;
        if (manager == null) {
            return;
        }
        while (nextToDecode < FRAME_COUNT
                && pending.size() < MAX_PREFETCH
                && nextToDecode - baseIndex < MAX_PREFETCH) {
            int index = nextToDecode++;
            CompletableFuture<NativeImage> future = CompletableFuture.supplyAsync(
                    () -> decode(manager, index), Util.backgroundExecutor());
            pending.addLast(new PendingFrame(index, future));
        }
    }

    private void uploadReady(int baseIndex) {
        int uploads = 0;
        while (uploads < MAX_UPLOADS_PER_TICK) {
            PendingFrame head = pending.peekFirst();
            if (head == null || !head.future().isDone()) {
                return;
            }
            pending.pollFirst();
            NativeImage image;
            try {
                image = head.future().join();
            } catch (CompletionException | CancellationException e) {
                handleFailure(head.index(), e);
                return;
            }
            if (head.index() < baseIndex) {
                image.close();
                continue;
            }
            upload(head.index(), image);
            uploads++;
        }
    }

    private void upload(int index, NativeImage image) {
        widths[index] = image.getWidth();
        heights[index] = image.getHeight();
        // The constructor uploads the pixels to the GPU, so the CPU copy can be closed right after.
        LinearTexture texture = new LinearTexture(TEXTURE_IDS[index], image);
        image.close();
        Minecraft.getInstance().getTextureManager().register(TEXTURE_IDS[index], texture);
        resident[index] = true;
        if (index == contiguousReady) {
            contiguousReady++;
        }
    }

    private void releasePassedFrames(int baseIndex) {
        int display = displayIndex(baseIndex);
        for (int i = 0; i < display; i++) {
            if (resident[i]) {
                free(i);
            }
        }
    }

    private int displayIndex(int index) {
        for (int i = Math.min(index, FRAME_COUNT - 1); i >= 0; i--) {
            if (resident[i]) {
                return i;
            }
        }
        return -1;
    }

    private void free(int index) {
        Minecraft.getInstance().getTextureManager().release(TEXTURE_IDS[index]);
        resident[index] = false;
    }

    private void handleFailure(int index, Throwable error) {
        failed = true;
        Throwable cause = error instanceof CompletionException && error.getCause() != null
                ? error.getCause() : error;
        String name = fileName(index);
        if (cause instanceof MissingFrameException) {
            RunningSkeletonMod.LOGGER.warn("Animation frame is missing: {}", name);
            LocalPlayer player = Minecraft.getInstance().player;
            if (player != null) {
                player.sendSystemMessage(Component.literal("[RunningSkeleton] Missing frame " + name
                        + " in assets/" + RunningSkeletonMod.MOD_ID + "/" + ASSET_DIRECTORY));
            }
        } else {
            RunningSkeletonMod.LOGGER.error("Failed to decode animation frame {}", name, cause);
        }
    }

    private static void discard(CompletableFuture<NativeImage> future) {
        future.whenComplete((image, error) -> {
            if (image != null) {
                image.close();
            }
        });
    }

    private static NativeImage decode(ResourceManager manager, int index) {
        Optional<Resource> resource = manager.getResource(SOURCE_IDS[index]);
        if (resource.isEmpty()) {
            throw new MissingFrameException(fileName(index));
        }
        try (InputStream stream = resource.get().open()) {
            return NativeImage.read(stream);
        } catch (IOException e) {
            throw new UncheckedIOException("Could not read " + fileName(index), e);
        }
    }

    /**
     * A frame ready to be drawn.
     *
     * @param texture the registered texture id
     * @param width   the frame width in pixels
     * @param height  the frame height in pixels
     */
    public record FrameView(Identifier texture, int width, int height) {
    }

    /** A decode job that is running or finished but not yet uploaded. */
    private record PendingFrame(int index, CompletableFuture<NativeImage> future) {
    }

    /** A dynamic texture sampled with linear filtering so scaled frames look smooth. */
    private static final class LinearTexture extends DynamicTexture {

        LinearTexture(Identifier id, NativeImage image) {
            super(id::toString, image);
        }

        @Override
        public void upload() {
            sampler = RenderSystem.getSamplerCache().getClampToEdge(FilterMode.LINEAR);
            super.upload();
        }
    }

    /** Signals that a frame PNG does not exist in the mod resources. */
    private static final class MissingFrameException extends RuntimeException {
        private static final long serialVersionUID = 1L;

        MissingFrameException(String fileName) {
            super("Missing animation frame " + fileName);
        }
    }
}
