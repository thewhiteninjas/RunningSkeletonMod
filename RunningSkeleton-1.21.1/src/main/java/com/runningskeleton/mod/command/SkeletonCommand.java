package com.runningskeleton.mod.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.runningskeleton.mod.client.overlay.SkeletonOverlay;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;

public class SkeletonCommand {

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(
            Commands.literal("skeleton")
                .requires(source -> source.hasPermission(2)) // OP level 2

                // /skeleton chance <number>
                .then(Commands.literal("chance")
                    .then(Commands.argument("oneIn", IntegerArgumentType.integer(1, 1000000))
                        .executes(SkeletonCommand::setChance)
                    )
                )

                // /skeleton show — force trigger the animation
                .then(Commands.literal("show")
                    .executes(SkeletonCommand::showNow)
                )

                // /skeleton info — display current config
                .then(Commands.literal("info")
                    .executes(SkeletonCommand::showInfo)
                )
        );
    }

    private static int setChance(CommandContext<CommandSourceStack> ctx) {
        int oneIn = IntegerArgumentType.getInteger(ctx, "oneIn");
        SkeletonOverlay.setChance(oneIn);
        ctx.getSource().sendSuccess(
            () -> Component.literal(
                "[RunningSkeleton] Probabilidad establecida: 1 de cada " + oneIn + " segundos."
            ),
            true
        );
        return 1;
    }

    private static int showNow(CommandContext<CommandSourceStack> ctx) {
        // Trigger on client thread
        net.minecraft.client.Minecraft.getInstance().execute(() -> {
            SkeletonOverlay.INSTANCE.triggerNow();
        });
        ctx.getSource().sendSuccess(
            () -> Component.literal("[RunningSkeleton] ¡Animación activada!"),
            false
        );
        return 1;
    }

    private static int showInfo(CommandContext<CommandSourceStack> ctx) {
        int chance = SkeletonOverlay.getChance();
        ctx.getSource().sendSuccess(
            () -> Component.literal(
                "[RunningSkeleton] Probabilidad actual: 1 de cada " + chance + " segundos. " +
                "Usa /skeleton chance <número> para cambiarla."
            ),
            false
        );
        return 1;
    }
}
