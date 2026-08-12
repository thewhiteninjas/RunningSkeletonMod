package com.thewhiteninja.runningskeleton.client.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.thewhiteninja.runningskeleton.client.animation.AnimationManager;
import com.thewhiteninja.runningskeleton.client.config.ModConfig;
import net.fabricmc.fabric.api.client.command.v2.ClientCommands;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.network.chat.Component;

/**
 * Registers the /skeleton command tree: chance, show and info.
 */
public final class SkeletonCommand {

	private SkeletonCommand() {
	}

	public static void register(CommandDispatcher<FabricClientCommandSource> dispatcher, AnimationManager animationManager) {
		dispatcher.register(ClientCommands.literal("skeleton")
				.then(ClientCommands.literal("chance")
						.then(ClientCommands.argument("probability", IntegerArgumentType.integer(1))
								.executes(context -> executeChance(
										context.getSource(),
										IntegerArgumentType.getInteger(context, "probability"),
										animationManager))))
				.then(ClientCommands.literal("show")
						.executes(context -> executeShow(context.getSource(), animationManager)))
				.then(ClientCommands.literal("info")
						.executes(context -> executeInfo(context.getSource(), animationManager))));
	}

	private static int executeChance(FabricClientCommandSource source, int probability, AnimationManager animationManager) {
		animationManager.setChanceDenominator(probability);

		ModConfig config = new ModConfig();
		config.setChanceDenominator(probability);
		config.save();

		source.sendFeedback(Component.literal("RunningSkeleton: probability set to 1/" + probability + "."));
		return 1;
	}

	private static int executeShow(FabricClientCommandSource source, AnimationManager animationManager) {
		animationManager.forceShow();
		source.sendFeedback(Component.literal("RunningSkeleton: showing animation now."));
		return 1;
	}

	private static int executeInfo(FabricClientCommandSource source, AnimationManager animationManager) {
		int denominator = animationManager.getChanceDenominator();
		boolean animating = animationManager.isCurrentlyAnimating();
		boolean cooldown = animationManager.isInCooldown();

		source.sendFeedback(Component.literal("RunningSkeleton configuration:"));
		source.sendFeedback(Component.literal("- Probability: 1/" + denominator + " per second."));
		source.sendFeedback(Component.literal("- Animation playing: " + animating));

		if (cooldown) {
			long remainingSeconds = (animationManager.getCooldownRemainingMillis() + 999L) / 1000L;
			source.sendFeedback(Component.literal("- Cooldown remaining: " + remainingSeconds + "s"));
		} else {
			source.sendFeedback(Component.literal("- Cooldown remaining: none"));
		}

		return 1;
	}
}
