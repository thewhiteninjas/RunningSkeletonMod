package com.thewhiteninja.runningskeleton;

import com.hypixel.hytale.server.core.plugin.JavaPlugin;
import com.hypixel.hytale.server.core.plugin.JavaPluginInit;
import com.hypixel.hytale.server.core.util.Config;
import com.thewhiteninja.runningskeleton.animation.SkeletonAnimationService;
import com.thewhiteninja.runningskeleton.commands.SkeletonCommand;
import com.thewhiteninja.runningskeleton.config.RunningSkeletonConfig;

import javax.annotation.Nonnull;

public class RunningSkeletonPlugin extends JavaPlugin {

    private final Config<RunningSkeletonConfig> config;
    private SkeletonAnimationService animationService;

    public RunningSkeletonPlugin(@Nonnull JavaPluginInit init) {
        super(init);
        this.config = this.withConfig("running_skeleton_config", RunningSkeletonConfig.CODEC);
    }

    @Override
    protected void setup() {
        this.config.save();

        this.animationService = new SkeletonAnimationService(this, this.config);
        this.getCommandRegistry().registerCommand(new SkeletonCommand(this.animationService, this.config));
    }

    @Override
    protected void start() {
        this.animationService.start();
    }

    @Override
    protected void shutdown() {
        if (this.animationService != null) {
            this.animationService.stop();
        }
    }
}
