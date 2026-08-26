package com.thewhiteninja.elephantgreenscreeneffect;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;

/**
 * Fabric client entrypoint. Mirrors the responsibilities of the original
 * Forge {@code @Mod} constructor: register the mod's sounds, load the
 * client config and hook up the animation/command logic. This mod is
 * client-only, exactly like the Forge version (side = "CLIENT").
 */
public final class ElephantGreenScreenEffectClient implements ClientModInitializer {

    public static final String MOD_ID = "elephantgreenscreeneffect";

    @Override
    public void onInitializeClient() {
        ModSounds.register();

        ClientConfig.load();

        ClientTickEvents.END_CLIENT_TICK.register(ElephantAnimationManager::onClientTick);
        HudRenderCallback.EVENT.register(ElephantAnimationManager::onRenderGui);

        ClientCommandRegistrationCallback.EVENT.register(ElephantClientCommand::register);
    }
}
