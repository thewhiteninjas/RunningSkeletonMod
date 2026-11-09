package com.thewhiteninja.runningskeleton.client;

import com.thewhiteninja.runningskeleton.RunningSkeletonMod;
import com.thewhiteninja.runningskeleton.client.animation.AnimationController;
import com.thewhiteninja.runningskeleton.client.command.SkeletonCommand;
import com.thewhiteninja.runningskeleton.client.config.ConfigManager;
import com.thewhiteninja.runningskeleton.client.render.HudAnimationRenderer;
import net.fabricmc.api.ClientModInitializer;

/** Client entrypoint: wires together configuration, controller, commands, rendering and events. */
public final class RunningSkeletonClient implements ClientModInitializer {

	@Override
	public void onInitializeClient() {
		ConfigManager config = new ConfigManager();
		config.load();

		AnimationController controller = new AnimationController(config);
		HudAnimationRenderer renderer = new HudAnimationRenderer(controller);

		SkeletonCommand.register(config, controller);
		ClientEvents.register(controller, renderer);

		RunningSkeletonMod.LOGGER.info("RunningSkeleton client initialised (probability 1 in {}).", config.getChance());
	}
}
