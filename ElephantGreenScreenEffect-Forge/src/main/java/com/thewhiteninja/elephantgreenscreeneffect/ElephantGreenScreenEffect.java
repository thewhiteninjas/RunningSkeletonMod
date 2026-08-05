package com.thewhiteninja.elephantgreenscreeneffect;

import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;


@Mod(ElephantGreenScreenEffect.MOD_ID)
public final class ElephantGreenScreenEffect {

    public static final String MOD_ID = "elephantgreenscreeneffect";

    public ElephantGreenScreenEffect() {
        ModSounds.SOUND_EVENTS.register(FMLJavaModLoadingContext.get().getModEventBus());

        ModLoadingContext.get().registerConfig(
                ModConfig.Type.CLIENT,
                ClientConfig.SPEC,
                MOD_ID + "-client.toml"
        );
    }
}
