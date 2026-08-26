package com.thewhiteninja.runningskeleton;

import com.mojang.blaze3d.platform.Window;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.resources.Identifier;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderGuiEvent;

/**
 * Draws the current animation frame stretched across the whole screen, adapting to the
 * window's current resolution and aspect ratio every time it renders.
 */
@EventBusSubscriber(modid = RunningSkeletonMod.MOD_ID, value = Dist.CLIENT)
public final class SkeletonOverlayRenderer {

    private static final int SOURCE_IMAGE_SIZE = 1024;

    private SkeletonOverlayRenderer() {
    }

    @SubscribeEvent
    static void onRenderGui(RenderGuiEvent.Post event) {
        if (!SkeletonAnimationState.isPlaying()) {
            return;
        }

        Identifier frame = SkeletonTextures.frame(SkeletonAnimationState.currentFrameIndex());
        Window window = Minecraft.getInstance().getWindow();
        int screenWidth = window.getGuiScaledWidth();
        int screenHeight = window.getGuiScaledHeight();

        GuiGraphicsExtractor graphics = event.getGuiGraphics();
        graphics.blit(
                RenderPipelines.GUI_TEXTURED,
                frame,
                0, 0,
                0.0F, 0.0F,
                screenWidth, screenHeight,
                SOURCE_IMAGE_SIZE, SOURCE_IMAGE_SIZE,
                SOURCE_IMAGE_SIZE, SOURCE_IMAGE_SIZE);
    }
}
