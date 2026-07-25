package com.thewhiteninja.runningskeleton.config;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;

public class RunningSkeletonConfig {

    public static final int DEFAULT_CHANCE = 2000;
    public static final int DEFAULT_COOLDOWN_SECONDS = 5;

    public static final BuilderCodec<RunningSkeletonConfig> CODEC = BuilderCodec.builder(RunningSkeletonConfig.class, RunningSkeletonConfig::new)
            .append(
                new KeyedCodec<>("Chance", Codec.INTEGER),
                (runningSkeletonConfig, value, extraInfo) -> runningSkeletonConfig.chance = value,
                (runningSkeletonConfig, extraInfo) -> runningSkeletonConfig.chance
            )
            .add()
            .append(
                new KeyedCodec<>("CooldownSeconds", Codec.INTEGER),
                (runningSkeletonConfig, value, extraInfo) -> runningSkeletonConfig.cooldownSeconds = value,
                (runningSkeletonConfig, extraInfo) -> runningSkeletonConfig.cooldownSeconds
            )
            .add()
            .build();

    private int chance;
    private int cooldownSeconds;

    private RunningSkeletonConfig() {
        this.chance = DEFAULT_CHANCE;
        this.cooldownSeconds = DEFAULT_COOLDOWN_SECONDS;
    }

    public int getChance() {
        return chance;
    }

    public void setChance(int chance) {
        this.chance = chance;
    }

    public int getCooldownSeconds() {
        return cooldownSeconds;
    }

    public void setCooldownSeconds(int cooldownSeconds) {
        this.cooldownSeconds = cooldownSeconds;
    }
}
