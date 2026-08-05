package com.thewhiteninja.elephantgreenscreeneffect;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;


public final class ModSounds {

    private ModSounds() {
    }

    public static final DeferredRegister<SoundEvent> SOUND_EVENTS =
            DeferredRegister.create(ForgeRegistries.SOUND_EVENTS, ElephantGreenScreenEffect.MOD_ID);

    public static final RegistryObject<SoundEvent> ELEPHANT_SOUND = SOUND_EVENTS.register(
            "elephant",
            () -> SoundEvent.createVariableRangeEvent(
                    new ResourceLocation(ElephantGreenScreenEffect.MOD_ID, "elephant"))
    );
}
