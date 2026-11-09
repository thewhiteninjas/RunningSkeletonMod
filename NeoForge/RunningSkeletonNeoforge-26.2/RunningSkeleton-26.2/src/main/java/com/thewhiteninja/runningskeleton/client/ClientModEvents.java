package com.thewhiteninja.runningskeleton.client;

import com.thewhiteninja.runningskeleton.RunningSkeletonMod;
import net.minecraft.resources.Identifier;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterGuiLayersEvent;

/**
 * Client events delivered on the mod event bus.
 */
@EventBusSubscriber(modid = RunningSkeletonMod.MOD_ID, value = Dist.CLIENT)
public final class ClientModEvents {

    private ClientModEvents() {
    }

    /**
     * Registers the animation layer above all other HUD layers.
     *
     * @param event the layer registration event
     */
    @SubscribeEvent
    public static void onRegisterGuiLayers(RegisterGuiLayersEvent event) {
        event.registerAboveAll(
                Identifier.fromNamespaceAndPath(RunningSkeletonMod.MOD_ID, "animation"),
                new AnimationLayer());
    }
}
