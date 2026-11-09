package com.thewhiteninja.runningskeleton.client;

import com.thewhiteninja.runningskeleton.client.animation.AnimationController;
import com.thewhiteninja.runningskeleton.client.render.HudAnimationRenderer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;

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
		HudRenderCallback.EVENT.register((context, tickCounter) -> renderer.render(context));
		ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> client.execute(controller::onDisconnect));
		ClientLifecycleEvents.CLIENT_STOPPING.register(client -> controller.shutdown());
	}
}
