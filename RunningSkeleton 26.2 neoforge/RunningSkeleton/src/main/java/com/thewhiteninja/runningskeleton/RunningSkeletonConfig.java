package com.thewhiteninja.runningskeleton;

import net.neoforged.neoforge.common.ModConfigSpec;

/**
 * Client-side configuration for RunningSkeleton.
 * Backed by a NeoForge {@link ModConfigSpec}, which persists changes to a TOML file
 * under the game's {@code config} directory and reloads it automatically on the next launch.
 */
public final class RunningSkeletonConfig {

    private static final int DEFAULT_CHANCE = 2000;

    private static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();

    private static final ModConfigSpec.IntValue CHANCE = BUILDER
            .comment(
                    "Probability denominator for the running skeleton animation.",
                    "Every second there is a 1 in <chance> probability of triggering the animation.",
                    "Change with /skeleton chance <number>. Minimum value is 1.")
            .defineInRange("chance", DEFAULT_CHANCE, 1, Integer.MAX_VALUE);

    public static final ModConfigSpec SPEC = BUILDER.build();

    private RunningSkeletonConfig() {
    }

    public static int getChance() {
        return CHANCE.get();
    }

    public static void setChance(int chance) {
        CHANCE.set(chance);
    }
}
