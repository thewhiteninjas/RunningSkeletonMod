package com.thewhiteninja.runningskeleton.client;

import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.systems.RenderSystem;
import com.thewhiteninja.runningskeleton.RunningSkeletonMod;
import com.thewhiteninja.runningskeleton.config.SkeletonConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Client-side state machine that decides when the animation appears, loads its frames, plays it
 * full screen and enforces the cooldown.
 *
 * <p>Frames are decoded on background threads and only the cheap GPU upload happens on the render
 * thread, a few frames per tick. All timing uses {@link System#nanoTime()}, so it does not depend
 * on the game tick rate or on the frame rate. Every method must be called from the client (render)
 * thread.</p>
 */
public final class AnimationController {

    /** Playback speed of the animation. */
    private static final double FRAMES_PER_SECOND = 30.0;

    private static final long NANOS_PER_SECOND = 1_000_000_000L;
    private static final long COOLDOWN_NANOS = SkeletonConfig.COOLDOWN_SECONDS * NANOS_PER_SECOND;

    /** Maximum frames uploaded to the GPU per client tick. */
    private static final int UPLOADS_PER_TICK = 3;

    /** Maximum number of frames decoded ahead of the last uploaded one (bounds memory use). */
    private static final int DECODE_LOOKAHEAD = 12;

    /** Playback starts once this many frames are uploaded; the rest keep loading during playback. */
    private static final int START_BUFFER_FRAMES = 8;

    private static AnimationState state = AnimationState.IDLE;
    private static long lastRollNanos = 0L;
    private static long playbackStartNanos = 0L;
    private static long cooldownEndNanos = 0L;
    private static String missingFramePath = null;
    private static boolean missingFramesLogged = false;

    /** Decode jobs indexed by frame; an entry is null once its frame has been uploaded. */
    private static final List<CompletableFuture<NativeImage>> PENDING = new ArrayList<>();
    private static int submittedFrames = 0;
    private static int uploadedFrames = 0;

    private AnimationController() {
    }

    /** Result of a request to start the animation. */
    public enum StartResult {
        /** The animation was started. */
        STARTED,
        /** The animation is already loading or playing. */
        ALREADY_ACTIVE,
        /** At least one frame PNG is missing from the mod resources. */
        FRAMES_MISSING
    }

    /** Returns the current state of the animation. */
    public static AnimationState getState() {
        return state;
    }

    /** Returns the remaining cooldown in seconds, or 0 if no cooldown is running. */
    public static double getCooldownRemainingSeconds() {
        if (state != AnimationState.COOLDOWN) {
            return 0.0;
        }
        long remaining = cooldownEndNanos - System.nanoTime();
        return Math.max(0L, remaining) / (double) NANOS_PER_SECOND;
    }

    /** Returns the path of the first missing frame found by the last failed start, if any. */
    public static String getMissingFramePath() {
        return missingFramePath;
    }

    /**
     * Starts the animation immediately, ignoring the random roll and any running cooldown.
     * Used by {@code /skeleton show}.
     */
    public static StartResult requestShow() {
        return begin(Minecraft.getInstance());
    }

    /** Advances the state machine. Called once per client tick while in a world. */
    public static void tick() {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.level == null || minecraft.player == null) {
            return;
        }

        long now = System.nanoTime();
        switch (state) {
            case IDLE -> rollAutomaticSpawn(minecraft, now);
            case COOLDOWN -> {
                if (now >= cooldownEndNanos) {
                    state = AnimationState.IDLE;
                    lastRollNanos = now;
                }
            }
            case LOADING -> {
                if (!pumpLoading(minecraft)) {
                    finishPlayback(minecraft, now);
                } else if (uploadedFrames >= START_BUFFER_FRAMES) {
                    state = AnimationState.PLAYING;
                    playbackStartNanos = now;
                }
            }
            case PLAYING -> {
                if (!pumpLoading(minecraft) || currentFrameIndex(now) >= AnimationFrames.FRAME_COUNT) {
                    finishPlayback(minecraft, now);
                }
            }
        }
    }

    /**
     * Draws the current frame filling the whole screen. Called from the GUI overlay.
     *
     * <p>The square frame is scaled uniformly so it covers the entire window; the parts that fall
     * outside the window are cropped. This keeps the aspect ratio and leaves no borders.</p>
     *
     * @param graphics the GUI graphics context
     * @param width    scaled GUI width
     * @param height   scaled GUI height
     */
    public static void render(GuiGraphics graphics, int width, int height) {
        if (state != AnimationState.PLAYING || uploadedFrames <= 0) {
            return;
        }

        int frame = currentFrameIndex(System.nanoTime());
        // Never draw a frame that is not uploaded yet; hold the latest available one instead.
        frame = Math.min(frame, Math.min(uploadedFrames, AnimationFrames.FRAME_COUNT) - 1);
        if (frame < 0) {
            return;
        }

        int side = Math.max(width, height);
        int x = (width - side) / 2;
        int y = (height - side) / 2;

        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        graphics.blit(AnimationFrames.getTexture(frame), x, y, side, side,
                0.0F, 0.0F,
                AnimationFrames.TEXTURE_SIZE, AnimationFrames.TEXTURE_SIZE,
                AnimationFrames.TEXTURE_SIZE, AnimationFrames.TEXTURE_SIZE);
        RenderSystem.disableBlend();
    }

    /** Stops everything and frees the textures. Called when leaving a world. */
    public static void reset() {
        Minecraft minecraft = Minecraft.getInstance();
        discardPendingDecodes();
        releaseTextures(minecraft);
        state = AnimationState.IDLE;
        lastRollNanos = 0L;
        playbackStartNanos = 0L;
        cooldownEndNanos = 0L;
    }

    private static void rollAutomaticSpawn(Minecraft minecraft, long now) {
        if (now - lastRollNanos < NANOS_PER_SECOND) {
            return;
        }
        lastRollNanos = now;

        int chance = SkeletonConfig.getChance();
        // nextInt(1) is always 0, so a chance of 1 means "every second".
        if (ThreadLocalRandom.current().nextInt(chance) != 0) {
            return;
        }

        StartResult result = begin(minecraft);
        if (result == StartResult.FRAMES_MISSING && !missingFramesLogged) {
            missingFramesLogged = true;
            RunningSkeletonMod.LOGGER.warn(
                    "Animation frame '{}' is missing; the animation cannot be shown.", missingFramePath);
        }
    }

    private static StartResult begin(Minecraft minecraft) {
        if (state == AnimationState.LOADING || state == AnimationState.PLAYING) {
            return StartResult.ALREADY_ACTIVE;
        }

        missingFramePath = AnimationFrames.findFirstMissing(minecraft.getResourceManager());
        if (missingFramePath != null) {
            return StartResult.FRAMES_MISSING;
        }

        PENDING.clear();
        submittedFrames = 0;
        uploadedFrames = 0;
        // Start decoding right away so the first frames are ready within a few ticks.
        fillDecodePipeline(minecraft);
        state = AnimationState.LOADING;
        return StartResult.STARTED;
    }

    /**
     * Uploads finished frames to the GPU (a few per tick) and keeps the background decoding busy.
     *
     * @return {@code false} if a frame failed to decode and the animation must be aborted
     */
    private static boolean pumpLoading(Minecraft minecraft) {
        int uploads = 0;
        while (uploadedFrames < submittedFrames && uploads < UPLOADS_PER_TICK) {
            CompletableFuture<NativeImage> job = PENDING.get(uploadedFrames);
            if (!job.isDone()) {
                break;
            }

            NativeImage image;
            try {
                image = job.join();
            } catch (CompletionException exception) {
                RunningSkeletonMod.LOGGER.error("Failed to decode animation frame {}.",
                        uploadedFrames + 1, exception);
                return false;
            }

            AnimationFrames.upload(minecraft.getTextureManager(), uploadedFrames, image);
            PENDING.set(uploadedFrames, null);
            uploadedFrames++;
            uploads++;
        }

        fillDecodePipeline(minecraft);
        return true;
    }

    private static void fillDecodePipeline(Minecraft minecraft) {
        while (submittedFrames < AnimationFrames.FRAME_COUNT
                && submittedFrames - uploadedFrames < DECODE_LOOKAHEAD) {
            PENDING.add(AnimationFrames.decodeAsync(minecraft.getResourceManager(), submittedFrames));
            submittedFrames++;
        }
    }

    private static int currentFrameIndex(long now) {
        double elapsedSeconds = (now - playbackStartNanos) / (double) NANOS_PER_SECOND;
        return (int) (elapsedSeconds * FRAMES_PER_SECOND);
    }

    private static void finishPlayback(Minecraft minecraft, long now) {
        discardPendingDecodes();
        releaseTextures(minecraft);
        state = AnimationState.COOLDOWN;
        cooldownEndNanos = now + COOLDOWN_NANOS;
    }

    /**
     * Closes images from decode jobs that were never uploaded. Jobs are not cancelled, because a
     * cancelled job would drop its image without closing it; the image is closed on completion.
     */
    private static void discardPendingDecodes() {
        for (CompletableFuture<NativeImage> job : PENDING) {
            if (job != null) {
                job.whenComplete((image, error) -> {
                    if (image != null) {
                        image.close();
                    }
                });
            }
        }
        PENDING.clear();
        submittedFrames = 0;
    }

    private static void releaseTextures(Minecraft minecraft) {
        if (uploadedFrames > 0) {
            AnimationFrames.release(minecraft.getTextureManager(), uploadedFrames);
            uploadedFrames = 0;
        }
    }
}
