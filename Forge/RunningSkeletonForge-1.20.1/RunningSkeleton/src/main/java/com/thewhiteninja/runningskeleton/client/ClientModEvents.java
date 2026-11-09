package com.thewhiteninja.runningskeleton.client;

import com.thewhiteninja.runningskeleton.RunningSkeletonMod;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterGuiOverlaysEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/** Mod-bus events, registered on the client only. */
@Mod.EventBusSubscriber(modid = RunningSkeletonMod.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public final class ClientModEvents {

    private ClientModEvents() {
    }

    /** Registers the full-screen overlay that draws the animation above the rest of the HUD. */
    @SubscribeEvent
    public static void onRegisterGuiOverlays(RegisterGuiOverlaysEvent event) {
        event.registerAboveAll("skeleton_animation",
                (gui, graphics, partialTick, width, height) -> drawAnimation(graphics, width, height));
    }

    private static void drawAnimation(GuiGraphics graphics, int width, int height) {
        AnimationController.render(graphics, width, height);
    }
}
