package com.thewhiteninja.runningskeleton.client;

import com.thewhiteninja.runningskeleton.RunningSkeletonMod;
import com.thewhiteninja.runningskeleton.client.animation.AnimationController;
import com.thewhiteninja.runningskeleton.client.render.HudAnimationRenderer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.minecraft.resources.Identifier;

/** Registers the client tick, HUD rendering and lifecycle (disconnect / shutdown) events. */
public final class ClientEvents {

	private ClientEvents() {
	}

	/**
	 * Registers all events.
	 *
	 * @param controller the animation controller
	 * @param renderer   the HUD renderer
	 */
	public static void register(AnimationController controller, HudAnimationRenderer renderer) {
		ClientTickEvents.END_CLIENT_TICK.register(controller::tick);
		// Added last so it is drawn after (over) every vanilla HUD element.
		HudElementRegistry.addLast(Identifier.fromNamespaceAndPath(RunningSkeletonMod.MOD_ID, "animation"),
				(graphics, deltaTracker) -> renderer.render(graphics));
		ClientPlayConnectionEvents.DISCONNECT.register((listener, client) -> client.execute(controller::onDisconnect));
		ClientLifecycleEvents.CLIENT_STOPPING.register(client -> controller.shutdown());
	}
}
