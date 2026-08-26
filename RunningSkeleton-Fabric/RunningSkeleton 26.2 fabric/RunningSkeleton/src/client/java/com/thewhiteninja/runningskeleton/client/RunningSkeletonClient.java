package com.thewhiteninja.runningskeleton.client;

import com.thewhiteninja.runningskeleton.client.animation.AnimationManager;
import com.thewhiteninja.runningskeleton.client.command.SkeletonCommand;
import com.thewhiteninja.runningskeleton.client.config.ModConfig;
import com.thewhiteninja.runningskeleton.client.render.SkeletonHudRenderer;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.minecraft.resources.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Client-side entrypoint for RunningSkeleton.
 * Wires together the animation state machine, the HUD renderer and the /skeleton commands.
 */
public final class RunningSkeletonClient implements ClientModInitializer {

	public static final String MOD_ID = "runningskeleton";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	private final AnimationManager animationManager = new AnimationManager();

	@Override
	public void onInitializeClient() {
		ModConfig config = ModConfig.load();
		animationManager.setChanceDenominator(config.chanceDenominator());

		ClientTickEvents.END_CLIENT_TICK.register(animationManager::onClientTick);

		SkeletonHudRenderer hudRenderer = new SkeletonHudRenderer(animationManager);
		HudElementRegistry.addLast(
				Identifier.fromNamespaceAndPath(MOD_ID, "skeleton_overlay"),
				hudRenderer::render
		);

		ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) ->
				SkeletonCommand.register(dispatcher, animationManager));

		LOGGER.info("RunningSkeleton initialized with chance 1/{}.", animationManager.getChanceDenominator());
	}
}
