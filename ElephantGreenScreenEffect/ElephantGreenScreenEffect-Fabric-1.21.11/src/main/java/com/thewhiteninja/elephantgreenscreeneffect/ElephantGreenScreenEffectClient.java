package com.thewhiteninja.elephantgreenscreeneffect;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.minecraft.resources.Identifier;

/**
 * Fabric client entrypoint. Mirrors the responsibilities of the original
 * Forge {@code @Mod} constructor: register the mod's sounds, load the
 * client config and hook up the animation/command logic. This mod is
 * client-only, exactly like the Forge version (side = "CLIENT").
 * <p>
 * HUD rendering is registered through {@code HudElementRegistry}, which
 * replaced the old {@code HudRenderCallback} in Fabric API when Minecraft's
 * HUD rendering was rewritten in 1.21.6 (HudRenderCallback is deprecated as
 * of that Fabric API branch and is not guaranteed to still be present here).
 */
public final class ElephantGreenScreenEffectClient implements ClientModInitializer {

    public static final String MOD_ID = "elephantgreenscreeneffect";

    @Override
    public void onInitializeClient() {
        ModSounds.register();

        ClientConfig.load();

        ClientTickEvents.END_CLIENT_TICK.register(ElephantAnimationManager::onClientTick);
        HudElementRegistry.addLast(
                Identifier.fromNamespaceAndPath(MOD_ID, "elephant_overlay"),
                ElephantAnimationManager::onRenderGui
        );

        ClientCommandRegistrationCallback.EVENT.register(ElephantClientCommand::register);
    }
}
