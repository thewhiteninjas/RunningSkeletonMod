package com.thewhiteninja.runningskeleton;

import static com.mojang.brigadier.arguments.IntegerArgumentType.getInteger;
import static com.mojang.brigadier.arguments.IntegerArgumentType.integer;
import static net.minecraft.commands.Commands.argument;
import static net.minecraft.commands.Commands.literal;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterClientCommandsEvent;

/**
 * Registers the {@code /skeleton} client command tree: {@code chance}, {@code show} and {@code info}.
 */
@EventBusSubscriber(modid = RunningSkeletonMod.MOD_ID, value = Dist.CLIENT)
public final class SkeletonCommand {

    private SkeletonCommand() {
    }

    @SubscribeEvent
    static void onRegisterClientCommands(RegisterClientCommandsEvent event) {
        CommandDispatcher<CommandSourceStack> dispatcher = event.getDispatcher();

        dispatcher.register(literal("skeleton")
                .then(literal("chance")
                        .then(argument("value", integer(1))
                                .executes(SkeletonCommand::runChance)))
                .then(literal("show").executes(SkeletonCommand::runShow))
                .then(literal("info").executes(SkeletonCommand::runInfo)));
    }

    private static int runChance(CommandContext<CommandSourceStack> context) {
        int value = getInteger(context, "value");
        RunningSkeletonConfig.setChance(value);
        context.getSource().sendSuccess(
                () -> Component.literal("RunningSkeleton: probability set to 1 in " + value + "."),
                false);
        return 1;
    }

    private static int runShow(CommandContext<CommandSourceStack> context) {
        SkeletonAnimationState.forceStart();
        context.getSource().sendSuccess(
                () -> Component.literal("RunningSkeleton: showing the animation now."),
                false);
        return 1;
    }

    private static int runInfo(CommandContext<CommandSourceStack> context) {
        int chance = RunningSkeletonConfig.getChance();
        String status = describeState();

        context.getSource().sendSuccess(
                () -> Component.literal(
                        "RunningSkeleton -> chance: 1 in " + chance + " per second, state: " + status),
                false);
        return 1;
    }

    private static String describeState() {
        if (SkeletonAnimationState.isPlaying()) {
            return "playing";
        }
        if (SkeletonAnimationState.isOnCooldown()) {
            return "cooldown (" + SkeletonAnimationState.remainingCooldownMillis() + " ms left)";
        }
        return "idle";
    }
}
