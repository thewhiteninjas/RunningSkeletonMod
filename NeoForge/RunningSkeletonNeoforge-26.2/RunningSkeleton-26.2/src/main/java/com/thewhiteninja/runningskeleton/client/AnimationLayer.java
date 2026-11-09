package com.thewhiteninja.runningskeleton.client;

import net.minecraft.client.DeltaTracker;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.RenderPipelines;
import net.neoforged.neoforge.client.gui.GuiLayer;

/**
 * HUD layer that draws the current animation frame over everything else.
 *
 * <p>A layer is not a screen, so it never captures input and the player keeps full control. The
 * frame is scaled to cover the whole window while keeping its proportions; whatever overflows the
 * window is cropped.</p>
 */
public final class AnimationLayer implements GuiLayer {

    @Override
    public void render(GuiGraphicsExtractor graphics, DeltaTracker deltaTracker) {
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

        graphics.pose().pushMatrix();
        graphics.pose().translate((width - scaledWidth) / 2f, (height - scaledHeight) / 2f);
        graphics.pose().scale(scale, scale);
        graphics.blit(RenderPipelines.GUI_TEXTURED, frame.texture(), 0, 0, 0f, 0f,
                frame.width(), frame.height(), frame.width(), frame.height());
        graphics.pose().popMatrix();
    }
}
