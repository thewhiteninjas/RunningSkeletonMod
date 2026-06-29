package com.runningskeleton.mod;

public class SkeletonAnimationState {

    private static boolean playing = false;
    private static int currentFrame = 0;
    private static long lastFrameTime = 0L;
    private static long cooldownEndTime = 0L;

    public static boolean isPlaying() {
        return playing;
    }

    public static boolean isInCooldown() {
        return System.currentTimeMillis() < cooldownEndTime;
    }

    public static void startAnimation() {
        playing = true;
        currentFrame = 0;
        lastFrameTime = System.currentTimeMillis();
    }

    public static void stopAnimation() {
        playing = false;
        currentFrame = 0;
        cooldownEndTime = System.currentTimeMillis() + SkeletonConfig.COOLDOWN_MS;
    }

    public static int getCurrentFrame() {
        return currentFrame;
    }

    /**
     * Advance frame if enough time has passed.
     * Returns true if the animation finished.
     */
    public static boolean tickFrame() {
        long now = System.currentTimeMillis();
        if (now - lastFrameTime >= SkeletonConfig.FRAME_DURATION_MS) {
            currentFrame++;
            lastFrameTime = now;
            if (currentFrame >= SkeletonConfig.TOTAL_FRAMES) {
                stopAnimation();
                return true;
            }
        }
        return false;
    }
}
