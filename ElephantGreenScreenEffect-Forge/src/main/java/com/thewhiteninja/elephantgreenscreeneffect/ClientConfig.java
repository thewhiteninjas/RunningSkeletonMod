package com.thewhiteninja.elephantgreenscreeneffect;

import net.minecraftforge.common.ForgeConfigSpec;


public final class ClientConfig {

    private ClientConfig() {
    }

    public static final ForgeConfigSpec SPEC;
    public static final ForgeConfigSpec.IntValue CHANCE;

    static {
        ForgeConfigSpec.Builder builder = new ForgeConfigSpec.Builder();

        builder.comment("ElephantGreenScreenEffect settings").push("general");

        CHANCE = builder
                .comment(
                        "Probability denominator checked once every second.",
                        "A value of 5000 means a 1 in 5000 chance of triggering the animation every second."
                )
                .defineInRange("chance", 2000, 1, Integer.MAX_VALUE);

        builder.pop();

        SPEC = builder.build();
    }
}
