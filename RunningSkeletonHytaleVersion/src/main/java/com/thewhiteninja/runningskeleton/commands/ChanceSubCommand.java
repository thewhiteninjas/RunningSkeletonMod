package com.thewhiteninja.runningskeleton.commands;

import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.command.system.AbstractCommand;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.arguments.system.RequiredArg;
import com.hypixel.hytale.server.core.command.system.arguments.types.ArgTypes;
import com.hypixel.hytale.server.core.util.Config;
import com.thewhiteninja.runningskeleton.config.RunningSkeletonConfig;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.concurrent.CompletableFuture;

public class ChanceSubCommand extends AbstractCommand {

    private final Config<RunningSkeletonConfig> config;
    private final RequiredArg<Integer> chanceArg =
            this.withRequiredArg("chance", "New chance denominator, as in 1 in X", ArgTypes.INTEGER);

    public ChanceSubCommand(@Nonnull Config<RunningSkeletonConfig> config) {
        super("chance", "Set the probability of the skeleton event, for example 2000 means 1 in 2000");
        this.config = config;
        this.requirePermission("runningskeleton.admin.chance");
    }

    @Override
    protected boolean canGeneratePermission() {
        return false;
    }

    @Nullable
    @Override
    protected CompletableFuture<Void> execute(@Nonnull CommandContext context) {
        int value = this.chanceArg.get(context);

        if (value < 1) {
            context.sendMessage(Message.raw("The chance value must be a positive integer.").color("#FF5555"));
            return CompletableFuture.completedFuture(null);
        }

        this.config.get().setChance(value);
        this.config.save();

        context.sendMessage(Message.raw("RunningSkeleton chance set to 1 in " + value + " per second.").color("#55FF55"));
        return CompletableFuture.completedFuture(null);
    }
}
