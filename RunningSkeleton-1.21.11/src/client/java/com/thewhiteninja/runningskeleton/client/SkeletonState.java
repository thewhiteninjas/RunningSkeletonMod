package com.thewhiteninja.runningskeleton.client;

/**
 * Holds the runtime state of the RunningSkeleton mod: the trigger chance,
 * the cooldown timer and whether the animation is currently playing.
 * <p>
 * This class is intentionally simple (no persistence) since the mod only
 * needs to keep its state for the current game session.
 */
public final class SkeletonState {

    /** Default chance denominator: 1 in 2000 per check. */
    public static final int DEFAULT_CHANCE = 2000;

    /** Cooldown duration in ticks after an animation finishes (5 seconds * 20 ticks/second). */
    public static final int COOLDOWN_TICKS = 5 * 20;

    /** Minecraft runs at 20 ticks per second; we want to check once per second. */
    public static final int TICKS_PER_CHECK = 20;

    private static int chance = DEFAULT_CHANCE;
    private static int cooldownTicksRemaining = 0;
    private static int tickAccumulator = 0;

    private SkeletonState() {
        // Utility class, no instances.
    }

    public static int getChance() {
        return chance;
    }

    /**
     * Sets a new chance denominator.
     *
     * @param newChance the new denominator (e.g. 2000 means 1 in 2000)
     */
    public static void setChance(int newChance) {
        chance = newChance;
    }

    public static boolean isOnCooldown() {
        return cooldownTicksRemaining > 0;
    }

    public static int getCooldownTicksRemaining() {
        return cooldownTicksRemaining;
    }

    public static double getCooldownSecondsRemaining() {
        return cooldownTicksRemaining / 20.0;
    }

    public static void startCooldown() {
        cooldownTicksRemaining = COOLDOWN_TICKS;
    }

    /**
     * Decreases the cooldown timer by one tick, if active.
     */
    public static void tickCooldown() {
        if (cooldownTicksRemaining > 0) {
            cooldownTicksRemaining--;
        }
    }

    /**
     * Accumulates ticks and returns true exactly once every {@link #TICKS_PER_CHECK} ticks,
     * signalling that a new random check should be performed.
     */
    public static boolean shouldPerformCheck() {
        tickAccumulator++;
        if (tickAccumulator >= TICKS_PER_CHECK) {
            tickAccumulator = 0;
            return true;
        }
        return false;
    }
}
