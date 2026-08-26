package com.runningskeleton.mod;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.minecraft.client.Minecraft;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterClientCommandsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = RunningSkeletonMod.MOD_ID, value = Dist.CLIENT)
public class SkeletonCommandHandler {

    @SubscribeEvent
    public static void onRegisterClientCommands(RegisterClientCommandsEvent event) {
        event.getDispatcher().register(
            Commands.literal("skeleton")
                .then(Commands.literal("show")
                    .executes(ctx -> {
                        Minecraft mc = Minecraft.getInstance();
                        if (mc.player == null) return 0;
                        if (SkeletonAnimationState.isPlaying()) {
                            mc.player.sendSystemMessage(Component.literal(
                                    "§e[RunningSkeleton] Animation is already playing!"));
                        } else {
                            SkeletonAnimationState.startAnimation();
                            mc.player.sendSystemMessage(Component.literal(
                                    "§a[RunningSkeleton] Showing animation!"));
                        }
                        return 1;
                    })
                )
                .then(Commands.literal("info")
                    .executes(ctx -> {
                        Minecraft mc = Minecraft.getInstance();
                        if (mc.player == null) return 0;
                        mc.player.sendSystemMessage(Component.literal("§b[RunningSkeleton] §fInfo:"));
                        mc.player.sendSystemMessage(Component.literal(
                                "§7 Chance: §f1 in " + SkeletonConfig.chance + " per second"));
                        mc.player.sendSystemMessage(Component.literal(
                            "§7 Frames: §f" + SkeletonConfig.TOTAL_FRAMES));
                        mc.player.sendSystemMessage(Component.literal(
                                "§7 Frame duration: §f" + SkeletonConfig.FRAME_DURATION_MS + "ms"));
                        mc.player.sendSystemMessage(Component.literal(
                            "§7 Cooldown: §f" + (SkeletonConfig.COOLDOWN_MS / 1000) + "s"));
                        mc.player.sendSystemMessage(Component.literal(
                                "§7 Playing: §f" + SkeletonAnimationState.isPlaying()));
                        mc.player.sendSystemMessage(Component.literal(
                            "§7 En cooldown: §f" + SkeletonAnimationState.isInCooldown()));
                        return 1;
                    })
                )
                .then(Commands.literal("chance")
                    .then(Commands.argument("number", IntegerArgumentType.integer(1))
                        .executes(ctx -> {
                            Minecraft mc = Minecraft.getInstance();
                            if (mc.player == null) return 0;
                            int val = IntegerArgumentType.getInteger(ctx, "number");
                            SkeletonConfig.chance = val;
                            mc.player.sendSystemMessage(Component.literal(
                                "§a[RunningSkeleton] Chance set to: 1 in " + val + " per second."));
                            return 1;
                        })
                    )
                )
                .executes(ctx -> {
                    Minecraft mc = Minecraft.getInstance();
                    if (mc.player == null) return 0;
                    mc.player.sendSystemMessage(Component.literal("§b[RunningSkeleton] Commands:"));
                    mc.player.sendSystemMessage(Component.literal("§7 /skeleton chance <numero>"));
                    mc.player.sendSystemMessage(Component.literal("§7 /skeleton show"));
                    mc.player.sendSystemMessage(Component.literal("§7 /skeleton info"));
                    return 1;
                })
        );
    }
}
