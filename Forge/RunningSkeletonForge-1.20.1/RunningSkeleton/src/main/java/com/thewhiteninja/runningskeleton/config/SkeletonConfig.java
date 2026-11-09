package com.thewhiteninja.runningskeleton.config;

import net.minecraftforge.common.ForgeConfigSpec;

/**
 * Persistent client configuration of the mod (config/runningskeleton-client.toml).
 *
 * <p>Only the spawn chance is user-configurable. The cooldown is a fixed constant.</p>
 */
public final class SkeletonConfig {

    /** Default value: a 1 in 2000 chance per second. */
    public static final int DEFAULT_CHANCE = 2000;

    /** Cooldown applied after an animation ends, in seconds. */
    public static final int COOLDOWN_SECONDS = 5;

    public static final ForgeConfigSpec SPEC;

    private static final ForgeConfigSpec.IntValue CHANCE;

    static {
        ForgeConfigSpec.Builder builder = new ForgeConfigSpec.Builder();
        builder.comment("RunningSkeleton client settings").push("animation");
        CHANCE = builder
                .comment("The animation has a 1 in <chance> probability of appearing every second.",
                        "Lower values make it appear more often. Minimum is 1 (always).")
                .defineInRange("chance", DEFAULT_CHANCE, 1, Integer.MAX_VALUE);
        builder.pop();
        SPEC = builder.build();
    }

    private SkeletonConfig() {
    }

    /**
     * Returns the currently configured chance denominator.
     *
     * @return a value between 1 and {@link Integer#MAX_VALUE}
     */
    public static int getChance() {
        if (!SPEC.isLoaded()) {
            return DEFAULT_CHANCE;
        }
        return CHANCE.get();
    }

    /**
     * Updates the chance denominator and immediately writes it to disk.
     *
     * @param chance the new denominator; values below 1 are clamped to 1
     */
    public static void setChance(int chance) {
        CHANCE.set(Math.max(1, chance));
        CHANCE.save();
    }
}
