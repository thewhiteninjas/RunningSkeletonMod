package com.thewhiteninja.runningskeleton.commands;

import com.hypixel.hytale.server.core.command.system.basecommands.AbstractCommandCollection;
import com.hypixel.hytale.server.core.util.Config;
import com.thewhiteninja.runningskeleton.animation.SkeletonAnimationService;
import com.thewhiteninja.runningskeleton.config.RunningSkeletonConfig;

import javax.annotation.Nonnull;

public class SkeletonCommand extends AbstractCommandCollection {

    public SkeletonCommand(@Nonnull SkeletonAnimationService animationService, @Nonnull Config<RunningSkeletonConfig> config) {
        super("skeleton", "Manage the RunningSkeleton plugin");
        this.addSubCommand(new ChanceSubCommand(config));
        this.addSubCommand(new ShowSubCommand(animationService));
        this.addSubCommand(new InfoSubCommand(animationService, config));
    }

    @Override
    protected boolean canGeneratePermission() {
        return false;
    }
}
