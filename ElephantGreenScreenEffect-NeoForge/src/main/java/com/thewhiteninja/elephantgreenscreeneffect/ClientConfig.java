package com.thewhiteninja.elephantgreenscreeneffect;

import net.neoforged.neoforge.common.ModConfigSpec;


public final class ClientConfig {

    private ClientConfig() {
    }

    public static final ModConfigSpec SPEC;
    public static final ModConfigSpec.IntValue CHANCE;

    static {
        ModConfigSpec.Builder builder = new ModConfigSpec.Builder();

        builder.comment("ElephantGreenScreenEffect settings").push("general");

        CHANCE = builder
                .comment(
                        "Probability denominator checked once every second.",
                        "A value of 5000 means a 1 in 5000 chance of triggering the elephant every second."
                )
                .defineInRange("chance", 5000, 1, Integer.MAX_VALUE);

        builder.pop();

        SPEC = builder.build();
    }
}
