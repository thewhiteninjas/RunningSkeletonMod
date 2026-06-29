package com.runningskeleton.mod;

public class SkeletonConfig {

    // 1 in X chance per second (default 2000)
    public static int chance = 2000;

    // Total frames in the animation
    public static final int TOTAL_FRAMES = 14;

    // Duration per frame in milliseconds (total animation ~1.4s at 100ms/frame)
    public static final long FRAME_DURATION_MS = 100L;

    // Cooldown after animation ends (ms)
    public static final long COOLDOWN_MS = 5000L;
}
