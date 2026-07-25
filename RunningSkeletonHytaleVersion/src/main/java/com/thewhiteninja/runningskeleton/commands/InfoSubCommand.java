package com.thewhiteninja.runningskeleton.commands;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.command.system.AbstractCommand;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.CommandSender;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.core.util.Config;
import com.thewhiteninja.runningskeleton.animation.SkeletonAnimationService;
import com.thewhiteninja.runningskeleton.config.RunningSkeletonConfig;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.concurrent.CompletableFuture;

public class InfoSubCommand extends AbstractCommand {

    private final SkeletonAnimationService animationService;
    private final Config<RunningSkeletonConfig> config;

    public InfoSubCommand(@Nonnull SkeletonAnimationService animationService, @Nonnull Config<RunningSkeletonConfig> config) {
        super("info", "Show the current RunningSkeleton probability, cooldown, and state");
        this.animationService = animationService;
        this.config = config;
    }

    @Nullable
    @Override
    protected CompletableFuture<Void> execute(@Nonnull CommandContext context) {
        RunningSkeletonConfig cfg = this.config.get();

        context.sendMessage(Message.raw("RunningSkeleton status").color("#FFD700"));
        context.sendMessage(Message.raw("Chance: 1 in " + cfg.getChance() + " per second"));
        context.sendMessage(Message.raw("Cooldown: " + cfg.getCooldownSeconds() + " second(s)"));

        CommandSender sender = context.sender();
        if (sender instanceof Player player) {
            Ref<EntityStore> ref = player.getReference();
            if (ref != null && ref.isValid()) {
                Store<EntityStore> store = ref.getStore();
                PlayerRef playerRef = store.getComponent(ref, PlayerRef.getComponentType());
                if (playerRef != null) {
                    boolean active = this.animationService.isOnCooldown(playerRef.getUuid());
                    context.sendMessage(Message.raw("Your cooldown is currently " + (active ? "active." : "not active.")));
                }
            }
        } else {
            int active = this.animationService.getActiveCooldownCount();
            context.sendMessage(Message.raw("Players currently on cooldown: " + active));
        }

        return CompletableFuture.completedFuture(null);
    }
}
