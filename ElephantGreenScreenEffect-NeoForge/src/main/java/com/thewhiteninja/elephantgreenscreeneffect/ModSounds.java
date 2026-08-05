package com.thewhiteninja.elephantgreenscreeneffect;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;


public final class ModSounds {

    private ModSounds() {
    }

    public static final DeferredRegister<SoundEvent> SOUND_EVENTS =
            DeferredRegister.create(Registries.SOUND_EVENT, ElephantGreenScreenEffect.MOD_ID);

    public static final DeferredHolder<SoundEvent, SoundEvent> ELEPHANT_SOUND = SOUND_EVENTS.register(
            "elephant",
            () -> SoundEvent.createVariableRangeEvent(
                    ResourceLocation.fromNamespaceAndPath(ElephantGreenScreenEffect.MOD_ID, "elephant"))
    );
}
