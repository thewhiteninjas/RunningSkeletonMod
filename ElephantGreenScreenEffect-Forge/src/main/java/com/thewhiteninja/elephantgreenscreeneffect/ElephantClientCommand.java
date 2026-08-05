package com.thewhiteninja.elephantgreenscreeneffect;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterClientCommandsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/**
 * Registers the /elephant client command with its three sub-commands:
 * chance, show and info.
 */
@Mod.EventBusSubscriber(modid = ElephantGreenScreenEffect.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
public final class ElephantClientCommand {

    private ElephantClientCommand() {
    }

    @SubscribeEvent
    public static void onRegisterCommands(RegisterClientCommandsEvent event) {
        event.getDispatcher().register(
                Commands.literal("elephant")
                        .then(Commands.literal("chance")
                                .then(Commands.argument("number", IntegerArgumentType.integer(1))
                                        .executes(ElephantClientCommand::setChance)))
                        .then(Commands.literal("show")
                                .executes(ElephantClientCommand::showAnimation))
                        .then(Commands.literal("info")
                                .executes(ElephantClientCommand::showInfo))
        );
    }

    private static int setChance(CommandContext<CommandSourceStack> context) {
        int value = IntegerArgumentType.getInteger(context, "number");

        ClientConfig.CHANCE.set(value);
        ClientConfig.SPEC.save();

        context.getSource().sendSuccess(
                () -> Component.literal("[ElephantGreenScreenEffect] Chance set to 1 in " + value + "."),
                false
        );

        return 1;
    }

    private static int showAnimation(CommandContext<CommandSourceStack> context) {
        boolean started = ElephantAnimationManager.startAnimation();

        if (started) {
            context.getSource().sendSuccess(
                    () -> Component.literal("[ElephantGreenScreenEffect] Triggering animation."),
                    false
            );
        } else {
            context.getSource().sendFailure(
                    Component.literal("[ElephantGreenScreenEffect] Could not start the animation. "
                            + "Check that the 71 frame_(N).png files and elephant.ogg are present "
                            + "(see latest.log for details) or that it is not already playing.")
            );
        }

        return started ? 1 : 0;
    }

    private static int showInfo(CommandContext<CommandSourceStack> context) {
        int chance = ClientConfig.CHANCE.get();

        context.getSource().sendSuccess(
                () -> Component.literal(
                        "[ElephantGreenScreenEffect] Current chance: 1 in " + chance + " per second."),
                false
        );

        return 1;
    }
}
