package com.thewhiteninja.runningskeleton.client;

import com.thewhiteninja.runningskeleton.RunningSkeletonMod;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.AddGuiOverlayLayersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/** Mod-bus events, registered on the client only. */
@Mod.EventBusSubscriber(modid = RunningSkeletonMod.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public final class ClientModEvents {

    /** Unique name of the animation layer inside Forge's layered GUI. */
    private static final ResourceLocation ANIMATION_LAYER =
            ResourceLocation.fromNamespaceAndPath(RunningSkeletonMod.MOD_ID, "skeleton_animation");

    private ClientModEvents() {
    }

    /**
     * Adds the animation as the last (topmost) layer of the in-game GUI. A layer is only drawn; it
     * never captures the mouse or the keyboard, so the player keeps full control.
     */
    @SubscribeEvent
    public static void onAddGuiOverlayLayers(AddGuiOverlayLayersEvent event) {
        event.getLayeredDraw().add(ANIMATION_LAYER,
                (graphics, deltaTracker) ->
                        AnimationController.render(graphics, graphics.guiWidth(), graphics.guiHeight()));
    }
}
