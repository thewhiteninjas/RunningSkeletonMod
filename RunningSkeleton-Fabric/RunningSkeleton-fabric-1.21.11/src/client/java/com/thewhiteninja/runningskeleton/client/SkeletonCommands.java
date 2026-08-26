package com.thewhiteninja.runningskeleton.client;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.context.CommandContext;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.text.Text;

/**
 * Registers the client-side "/skeleton" command and its subcommands:
 * <ul>
 *     <li>/skeleton chance &lt;number&gt; - sets the trigger chance denominator</li>
 *     <li>/skeleton show - immediately plays the animation, ignoring chance and cooldown checks</li>
 *     <li>/skeleton info - shows the current chance and cooldown status</li>
 * </ul>
 */
public final class SkeletonCommands {

    private SkeletonCommands() {
        // Utility class, no instances.
    }

    public static void register() {
        ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) ->
                dispatcher.register(ClientCommandManager.literal("skeleton")
                        .then(ClientCommandManager.literal("chance")
                                .then(ClientCommandManager.argument("number", IntegerArgumentType.integer(1))
                                        .executes(SkeletonCommands::executeChance)))
                        .then(ClientCommandManager.literal("show")
                                .executes(SkeletonCommands::executeShow))
                        .then(ClientCommandManager.literal("info")
                                .executes(SkeletonCommands::executeInfo))
                )
        );
    }

    private static int executeChance(CommandContext<FabricClientCommandSource> context) {
        int newChance = IntegerArgumentType.getInteger(context, "number");
        SkeletonState.setChance(newChance);

        context.getSource().sendFeedback(
                Text.literal("RunningSkeleton: chance set to 1 in " + newChance + ".")
        );
        return 1;
    }

    private static int executeShow(CommandContext<FabricClientCommandSource> context) {
        AnimationHandler.start();

        context.getSource().sendFeedback(
                Text.literal("RunningSkeleton: playing animation now.")
        );
        return 1;
    }

    private static int executeInfo(CommandContext<FabricClientCommandSource> context) {
        int chance = SkeletonState.getChance();

        String cooldownMessage;
        if (SkeletonState.isOnCooldown()) {
            cooldownMessage = String.format("on cooldown (%.1fs remaining)", SkeletonState.getCooldownSecondsRemaining());
        } else {
            cooldownMessage = "ready";
        }

        context.getSource().sendFeedback(
                Text.literal(String.format(
                        "RunningSkeleton: chance = 1 in %d, cooldown status = %s.",
                        chance,
                        cooldownMessage
                ))
        );
        return 1;
    }
}
