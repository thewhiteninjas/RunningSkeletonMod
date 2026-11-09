package com.thewhiteninja.runningskeleton.client;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.LayeredDraw;

/**
 * HUD layer that draws the current animation frame over everything else.
 *
 * <p>A layer is not a screen, so it never captures input and the player keeps full control. The
 * frame is scaled to cover the whole window while keeping its proportions; whatever
 * overflows the window is cropped by the viewport.</p>
 */
public final class AnimationLayer implements LayeredDraw.Layer {

    @Override
    public void render(GuiGraphics graphics, DeltaTracker deltaTracker) {
        FrameManager.FrameView frame = AnimationController.get().currentFrame();
        if (frame == null) {
            return;
        }
        int width = graphics.guiWidth();
        int height = graphics.guiHeight();
        // "Cover" scaling: the larger of the two ratios fills the window, the overflow is cropped.
        float scale = Math.max(width / (float) frame.width(), height / (float) frame.height());
        float scaledWidth = frame.width() * scale;
        float scaledHeight = frame.height() * scale;

        PoseStack pose = graphics.pose();
        pose.pushPose();
        pose.translate((width - scaledWidth) / 2f, (height - scaledHeight) / 2f, 0f);
        pose.scale(scale, scale, 1f);
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        graphics.blit(frame.texture(), 0, 0, 0f, 0f,
                frame.width(), frame.height(), frame.width(), frame.height());
        pose.popPose();
    }
}
