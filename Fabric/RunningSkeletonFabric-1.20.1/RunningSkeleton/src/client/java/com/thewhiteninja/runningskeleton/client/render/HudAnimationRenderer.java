package com.thewhiteninja.runningskeleton.client.render;

import com.thewhiteninja.runningskeleton.client.animation.AnimationController;
import com.thewhiteninja.runningskeleton.client.frames.GpuFrame;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.GuiGraphics;

/**
 * Draws the current frame over the whole window during HUD rendering.
 *
 * <p>The image is scaled uniformly to cover the window and the overflow is cropped by selecting
 * a centred region of the source texture, so the picture is never stretched and no bars appear.
 * It is not a screen, so input and gameplay are not affected.</p>
 */
public final class HudAnimationRenderer {

	private final AnimationController controller;

	/**
	 * Creates the renderer.
	 *
	 * @param controller the controller supplying the current frame
	 */
	public HudAnimationRenderer(AnimationController controller) {
		this.controller = controller;
	}

	/**
	 * Renders the current frame, if any.
	 *
	 * @param context the HUD draw context
	 */
	public void render(GuiGraphics context) {
		GpuFrame frame = controller.currentFrame();
		if (frame == null) {
			return;
		}
		int windowWidth = context.guiWidth();
		int windowHeight = context.guiHeight();
		int textureWidth = frame.width();
		int textureHeight = frame.height();
		if (windowWidth <= 0 || windowHeight <= 0 || textureWidth <= 0 || textureHeight <= 0) {
			return;
		}

		// "Cover" scaling: the scale that fills both axes, then crop the excess symmetrically.
		float scale = Math.max((float) windowWidth / textureWidth, (float) windowHeight / textureHeight);
		int regionWidth = Math.min(textureWidth, Math.max(1, Math.round(windowWidth / scale)));
		int regionHeight = Math.min(textureHeight, Math.max(1, Math.round(windowHeight / scale)));
		float u = (textureWidth - regionWidth) / 2.0F;
		float v = (textureHeight - regionHeight) / 2.0F;

		RenderSystem.enableBlend();
		RenderSystem.defaultBlendFunc();
		context.blit(frame.id(), 0, 0, windowWidth, windowHeight, u, v,
				regionWidth, regionHeight, textureWidth, textureHeight);
	}
}
