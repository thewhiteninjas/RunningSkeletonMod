package com.thewhiteninja.runningskeleton.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.thewhiteninja.runningskeleton.SkeletonConfig;
import com.thewhiteninja.runningskeleton.client.AnimationController;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;

/**
 * The client-side {@code /skeleton} command tree.
 */
public final class SkeletonCommands {

    private static final String VALUE_ARGUMENT = "value";

    private SkeletonCommands() {
    }

    /**
     * Registers {@code /skeleton chance [value]}, {@code /skeleton show} and {@code /skeleton info}.
     *
     * @param dispatcher the client command dispatcher
     */
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("skeleton")
                .then(Commands.literal("chance")
                        .executes(SkeletonCommands::showChance)
                        .then(Commands.argument(VALUE_ARGUMENT, IntegerArgumentType.integer(1))
                                .executes(SkeletonCommands::setChance)))
                .then(Commands.literal("show")
                        .executes(SkeletonCommands::show))
                .then(Commands.literal("info")
                        .executes(SkeletonCommands::info)));
    }

    private static int showChance(CommandContext<CommandSourceStack> context) {
        int chance = SkeletonConfig.getChance();
        context.getSource().sendSuccess(
                () -> Component.literal("Current chance: 1 in " + chance + " every second."), false);
        return chance;
    }

    private static int setChance(CommandContext<CommandSourceStack> context) {
        int value = IntegerArgumentType.getInteger(context, VALUE_ARGUMENT);
        SkeletonConfig.setChance(value);
        context.getSource().sendSuccess(
                () -> Component.literal("Chance set to 1 in " + value + " every second."), false);
        return value;
    }

    private static int show(CommandContext<CommandSourceStack> context) {
        if (AnimationController.get().show()) {
            return 1;
        }
        context.getSource().sendFailure(Component.literal("The animation is already loading or playing."));
        return 0;
    }

    private static int info(CommandContext<CommandSourceStack> context) {
        AnimationController controller = AnimationController.get();
        String message = "Chance: 1 in " + SkeletonConfig.getChance() + " every second\n"
                + "Cooldown: " + AnimationController.COOLDOWN_SECONDS + " s\n"
                + "State: " + controller.describeState();
        context.getSource().sendSuccess(() -> Component.literal(message), false);
        return 1;
    }
}
