package com.thewhiteninja.elephantgreenscreeneffect;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;

/**
 * Registers the mod's sound event directly against the vanilla sound-event
 * registry. Fabric has no equivalent to Forge's {@code DeferredRegister} /
 * {@code RegistryObject}, so registration happens with a plain
 * {@code Registry.register} call instead - the resulting {@link SoundEvent}
 * is otherwise identical.
 * <p>
 * Since 1.21, {@code ResourceLocation}'s constructor is private, so ids are
 * built with {@code ResourceLocation.fromNamespaceAndPath(...)} instead of
 * {@code new ResourceLocation(...)}.
 */
public final class ModSounds {

    private ModSounds() {
    }

    public static final ResourceLocation ELEPHANT_SOUND_ID =
            ResourceLocation.fromNamespaceAndPath(ElephantGreenScreenEffectClient.MOD_ID, "elephant");

    public static SoundEvent ELEPHANT_SOUND;

    public static void register() {
        ELEPHANT_SOUND = Registry.register(
                BuiltInRegistries.SOUND_EVENT,
                ELEPHANT_SOUND_ID,
                SoundEvent.createVariableRangeEvent(ELEPHANT_SOUND_ID)
        );
    }
}
