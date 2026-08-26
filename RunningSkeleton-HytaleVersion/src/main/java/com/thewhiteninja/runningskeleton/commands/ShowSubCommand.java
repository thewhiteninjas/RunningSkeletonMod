package com.thewhiteninja.runningskeleton.commands;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractPlayerCommand;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.thewhiteninja.runningskeleton.animation.SkeletonAnimationService;

import javax.annotation.Nonnull;

public class ShowSubCommand extends AbstractPlayerCommand {

    private final SkeletonAnimationService animationService;

    public ShowSubCommand(@Nonnull SkeletonAnimationService animationService) {
        super("show", "Immediately play the skeleton animation, ignoring the random chance");
        this.animationService = animationService;
    }

    @Override
    protected void execute(@Nonnull CommandContext context, @Nonnull Store<EntityStore> store,
                            @Nonnull Ref<EntityStore> ref, @Nonnull PlayerRef playerRef, @Nonnull World world) {
        this.animationService.forcePlay(playerRef, store, ref, world);
        context.sendMessage(Message.raw("Playing the RunningSkeleton animation now.").color("#55FF55"));
    }
}
