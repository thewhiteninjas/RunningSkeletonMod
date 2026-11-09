package com.thewhiteninja.runningskeleton;

import net.neoforged.neoforge.common.ModConfigSpec;

/**
 * Client configuration, stored in {@code runningskeleton-client.toml}.
 */
public final class SkeletonConfig {

    /** Default value of the chance denominator. */
    public static final int DEFAULT_CHANCE = 2000;

    private static final ModConfigSpec.IntValue CHANCE;

    /** The built specification, registered by {@link RunningSkeletonMod}. */
    public static final ModConfigSpec SPEC;

    static {
        ModConfigSpec.Builder builder = new ModConfigSpec.Builder();
        CHANCE = builder
                .comment("Every second there is a 1 in N chance that the animation is shown. N is this value.")
                .defineInRange("chance", DEFAULT_CHANCE, 1, Integer.MAX_VALUE);
        SPEC = builder.build();
    }

    private SkeletonConfig() {
    }

    /**
     * Returns the current chance denominator.
     *
     * @return N, where the per-second probability is 1/N
     */
    public static int getChance() {
        return CHANCE.get();
    }

    /**
     * Changes the chance denominator and writes the configuration file to disk.
     *
     * @param value the new denominator, must be at least 1
     */
    public static void setChance(int value) {
        CHANCE.set(value);
        CHANCE.save();
    }
}
