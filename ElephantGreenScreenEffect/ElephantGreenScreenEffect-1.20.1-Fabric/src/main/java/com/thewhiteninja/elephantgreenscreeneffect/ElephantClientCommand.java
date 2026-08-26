package com.thewhiteninja.elephantgreenscreeneffect;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.context.CommandContext;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.network.chat.Component;

import static net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.argument;
import static net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.literal;

/**
 * Registers the /elephant client command with its three sub-commands:
 * chance, show and info. Ported from Forge's
 * {@code RegisterClientCommandsEvent} to Fabric API's
 * {@code ClientCommandRegistrationCallback}; the command tree, argument
 * names and feedback messages are unchanged.
 */
public final class ElephantClientCommand {

    private ElephantClientCommand() {
    }

    public static void register(CommandDispatcher<FabricClientCommandSource> dispatcher,
                                 CommandBuildContext registryAccess) {
        dispatcher.register(
                literal("elephant")
                        .then(literal("chance")
                                .then(argument("number", IntegerArgumentType.integer(1))
                                        .executes(ElephantClientCommand::setChance)))
                        .then(literal("show")
                                .executes(ElephantClientCommand::showAnimation))
                        .then(literal("info")
                                .executes(ElephantClientCommand::showInfo))
        );
    }

    private static int setChance(CommandContext<FabricClientCommandSource> context) {
        int value = IntegerArgumentType.getInteger(context, "number");

        ClientConfig.setChance(value);

        context.getSource().sendFeedback(
                Component.literal("[ElephantGreenScreenEffect] Chance set to 1 in " + value + "."));

        return 1;
    }

    private static int showAnimation(CommandContext<FabricClientCommandSource> context) {
        boolean started = ElephantAnimationManager.startAnimation();

        if (started) {
            context.getSource().sendFeedback(
                    Component.literal("[ElephantGreenScreenEffect] Triggering animation."));
        } else {
            context.getSource().sendError(
                    Component.literal("[ElephantGreenScreenEffect] Could not start the animation. "
                            + "Check that the 71 frame_(N).png files and elephant.ogg are present "
                            + "(see latest.log for details) or that it is not already playing."));
        }

        return started ? 1 : 0;
    }

    private static int showInfo(CommandContext<FabricClientCommandSource> context) {
        int chance = ClientConfig.getChance();

        context.getSource().sendFeedback(
                Component.literal("[ElephantGreenScreenEffect] Current chance: 1 in " + chance + " per second."));

        return 1;
    }
}
