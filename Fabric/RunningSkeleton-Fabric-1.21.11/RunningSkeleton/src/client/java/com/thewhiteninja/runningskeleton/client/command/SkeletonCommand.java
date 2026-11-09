package com.thewhiteninja.runningskeleton.client.command;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.thewhiteninja.runningskeleton.client.animation.AnimationController;
import com.thewhiteninja.runningskeleton.client.animation.AnimationState;
import com.thewhiteninja.runningskeleton.client.config.ConfigManager;
import com.thewhiteninja.runningskeleton.client.frames.FrameTextureManager;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.text.Text;

import java.util.Locale;

/** Registers the client-side {@code /skeleton} command (never sent to the server). */
public final class SkeletonCommand {

	private SkeletonCommand() {
	}

	/**
	 * Registers the command tree.
	 *
	 * @param config     the configuration manager
	 * @param controller the animation controller
	 */
	public static void register(ConfigManager config, AnimationController controller) {
		ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) ->
				dispatcher.register(ClientCommandManager.literal("skeleton")
						.then(ClientCommandManager.literal("chance")
								.executes(context -> showChance(context, config))
								.then(ClientCommandManager.argument("number", IntegerArgumentType.integer(1))
										.executes(context -> setChance(context, config))))
						.then(ClientCommandManager.literal("show")
								.executes(context -> show(context, controller)))
						.then(ClientCommandManager.literal("info")
								.executes(context -> info(context, config, controller)))));
	}

	private static int showChance(CommandContext<FabricClientCommandSource> context, ConfigManager config) {
		context.getSource().sendFeedback(Text.literal(
				"Current probability: 1 in " + config.getChance() + " per second."));
		return 1;
	}

	private static int setChance(CommandContext<FabricClientCommandSource> context, ConfigManager config) {
		int value = IntegerArgumentType.getInteger(context, "number");
		if (config.setChance(value)) {
			context.getSource().sendFeedback(Text.literal("Probability set to 1 in " + value + " per second."));
		} else {
			context.getSource().sendError(Text.literal(
					"Probability set to 1 in " + value + " for this session, but it could not be saved to disk."));
		}
		return 1;
	}

	private static int show(CommandContext<FabricClientCommandSource> context, AnimationController controller) {
		controller.forceStart(context.getSource().getClient());
		context.getSource().sendFeedback(Text.literal("Playing animation."));
		return 1;
	}

	private static int info(CommandContext<FabricClientCommandSource> context, ConfigManager config,
							AnimationController controller) {
		FabricClientCommandSource source = context.getSource();
		AnimationState state = controller.getState();
		source.sendFeedback(Text.literal("Probability: 1 in " + config.getChance() + " per second"));
		source.sendFeedback(Text.literal("Cooldown: " + AnimationController.COOLDOWN_SECONDS + " seconds"));
		source.sendFeedback(Text.literal("State: " + state.label()));
		if (state == AnimationState.COOLDOWN) {
			source.sendFeedback(Text.literal(String.format(Locale.ROOT, "Remaining cooldown: %.1f seconds",
					controller.getCooldownRemainingMillis() / 1000.0)));
		}
		source.sendFeedback(Text.literal("Frames: " + FrameTextureManager.TOTAL_FRAMES));
		return 1;
	}
}
