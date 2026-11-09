package com.thewhiteninja.runningskeleton.client;

import com.mojang.blaze3d.platform.NativeImage;
import com.thewhiteninja.runningskeleton.RunningSkeletonMod;
import net.minecraft.Util;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;

import java.io.IOException;
import java.io.InputStream;
import java.util.Locale;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;

/**
 * Describes and manages the 79 animation frames.
 *
 * <p>Source PNGs live in {@code assets/runningskeleton/textures/animation/frame_0001.png} to
 * {@code frame_0079.png}. They are decoded on background threads and then uploaded to the GPU on
 * the render thread, so playback never stalls the game. Textures are released after playback so
 * that roughly 300 MB of 1024x1024 images do not stay in video memory permanently.</p>
 */
final class AnimationFrames {

    /** Total number of frames in the animation. */
    static final int FRAME_COUNT = 79;

    /** Width and height of every frame, in pixels. */
    static final int TEXTURE_SIZE = 1024;

    /** Where the PNG files are read from. */
    private static final ResourceLocation[] SOURCES = new ResourceLocation[FRAME_COUNT];

    /** Runtime texture names that the uploaded frames are registered under. */
    private static final ResourceLocation[] TEXTURES = new ResourceLocation[FRAME_COUNT];

    static {
        for (int i = 0; i < FRAME_COUNT; i++) {
            String source = String.format(Locale.ROOT, "textures/animation/frame_%04d.png", i + 1);
            String texture = String.format(Locale.ROOT, "animation/frame_%04d", i + 1);
            SOURCES[i] = ResourceLocation.fromNamespaceAndPath(RunningSkeletonMod.MOD_ID, source);
            TEXTURES[i] = ResourceLocation.fromNamespaceAndPath(RunningSkeletonMod.MOD_ID, texture);
        }
    }

    private AnimationFrames() {
    }

    /**
     * Returns the runtime texture location of a frame, to be used when drawing.
     *
     * @param index zero-based frame index
     */
    static ResourceLocation getTexture(int index) {
        return TEXTURES[index];
    }

    /**
     * Finds the first frame that is missing from the loaded resources.
     *
     * @return the path of the first missing frame, or {@code null} if every frame exists
     */
    static String findFirstMissing(ResourceManager resourceManager) {
        for (ResourceLocation source : SOURCES) {
            if (resourceManager.getResource(source).isEmpty()) {
                return source.getPath();
            }
        }
        return null;
    }

    /**
     * Decodes a frame PNG on a background thread.
     *
     * <p>The returned image is owned by the caller, who must either pass it to
     * {@link #upload} or close it.</p>
     *
     * @param index zero-based frame index
     */
    static CompletableFuture<NativeImage> decodeAsync(ResourceManager resourceManager, int index) {
        return CompletableFuture.supplyAsync(() -> {
            try (InputStream stream = resourceManager.getResource(SOURCES[index]).orElseThrow().open()) {
                return NativeImage.read(stream);
            } catch (IOException exception) {
                throw new CompletionException(exception);
            }
        }, Util.backgroundExecutor());
    }

    /**
     * Uploads a decoded frame to the GPU and registers it. The image is closed by the upload.
     * Must be called on the render thread.
     *
     * @param index zero-based frame index
     */
    static void upload(TextureManager textureManager, int index, NativeImage image) {
        textureManager.register(TEXTURES[index], new FrameTexture(image));
    }

    /**
     * Releases the first {@code uploadedCount} frames from video memory. Must be called on the
     * render thread.
     */
    static void release(TextureManager textureManager, int uploadedCount) {
        int count = Math.min(uploadedCount, FRAME_COUNT);
        for (int i = 0; i < count; i++) {
            textureManager.release(TEXTURES[i]);
        }
    }
}
