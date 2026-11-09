package com.thewhiteninja.runningskeleton.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.thewhiteninja.runningskeleton.client.AnimationController;
import com.thewhiteninja.runningskeleton.client.AnimationState;
import com.thewhiteninja.runningskeleton.config.SkeletonConfig;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;

import java.util.Locale;

/**
 * Client-side {@code /skeleton} command.
 *
 * <pre>
 * /skeleton chance [value]   shows or changes the 1-in-N spawn chance (saved automatically)
 * /skeleton show             plays the animation right now
 * /skeleton info             prints the current settings and animation state
 * </pre>
 */
public final class SkeletonCommand {

    private static final String PREFIX = "[RunningSkeleton] ";
    private static final String VALUE_ARGUMENT = "value";

    private SkeletonCommand() {
    }

    /** Registers the command tree on the given (client) dispatcher. */
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("skeleton")
                .then(Commands.literal("chance")
                        .executes(SkeletonCommand::showChance)
                        .then(Commands.argument(VALUE_ARGUMENT, IntegerArgumentType.integer(1))
                                .executes(SkeletonCommand::setChance)))
                .then(Commands.literal("show")
                        .executes(SkeletonCommand::show))
                .then(Commands.literal("info")
                        .executes(SkeletonCommand::info)));
    }

    private static int showChance(CommandContext<CommandSourceStack> context) {
        int chance = SkeletonConfig.getChance();
        reply(context, "Current chance: 1 in " + chance + " per second. Use /skeleton chance <number> to change it.");
        return chance;
    }

    private static int setChance(CommandContext<CommandSourceStack> context) {
        // The argument type already rejects non-numbers and values below 1.
        int value = IntegerArgumentType.getInteger(context, VALUE_ARGUMENT);
        SkeletonConfig.setChance(value);
        reply(context, "Chance set to 1 in " + value + " per second (saved).");
        return value;
    }

    private static int show(CommandContext<CommandSourceStack> context) {
        AnimationController.StartResult result = AnimationController.requestShow();
        switch (result) {
            case STARTED -> {
                reply(context, "Playing the animation.");
                return 1;
            }
            case ALREADY_ACTIVE -> {
                fail(context, "The animation is already running.");
                return 0;
            }
            default -> {
                fail(context, "Animation frame missing: " + AnimationController.getMissingFramePath()
                        + ". Place frame_0001.png to frame_0079.png in assets/runningskeleton/textures/animation/.");
                return 0;
            }
        }
    }

    private static int info(CommandContext<CommandSourceStack> context) {
        int chance = SkeletonConfig.getChance();
        AnimationState state = AnimationController.getState();

        String stateText = state.getDisplayName();
        if (state == AnimationState.COOLDOWN) {
            stateText += String.format(Locale.ROOT, " (%.1f s remaining)",
                    AnimationController.getCooldownRemainingSeconds());
        }

        reply(context, "Current settings:");
        reply(context, "  Chance: 1 in " + chance + " per second ("
                + String.format(Locale.ROOT, "%.4f", 100.0 / chance) + "%)");
        reply(context, "  Cooldown: " + SkeletonConfig.COOLDOWN_SECONDS + " seconds");
        reply(context, "  Animation: " + stateText);
        return 1;
    }

    private static void reply(CommandContext<CommandSourceStack> context, String message) {
        context.getSource().sendSuccess(() -> Component.literal(PREFIX + message), false);
    }

    private static void fail(CommandContext<CommandSourceStack> context, String message) {
        context.getSource().sendFailure(Component.literal(PREFIX + message));
    }
}
