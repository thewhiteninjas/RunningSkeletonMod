package com.thewhiteninja.elephantgreenscreeneffect;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;


@Mod(ElephantGreenScreenEffect.MOD_ID)
public final class ElephantGreenScreenEffect {

    public static final String MOD_ID = "elephantgreenscreeneffect";

    public ElephantGreenScreenEffect(IEventBus modEventBus, ModContainer modContainer) {
        ModSounds.SOUND_EVENTS.register(modEventBus);

        modContainer.registerConfig(
                ModConfig.Type.CLIENT,
                ClientConfig.SPEC,
                MOD_ID + "-client.toml"
        );
    }
}
