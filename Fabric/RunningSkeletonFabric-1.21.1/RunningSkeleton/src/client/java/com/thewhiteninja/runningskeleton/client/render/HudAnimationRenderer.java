package com.thewhiteninja.runningskeleton.client.render;

import com.mojang.blaze3d.systems.RenderSystem;
import com.thewhiteninja.runningskeleton.client.animation.AnimationController;
import com.thewhiteninja.runningskeleton.client.frames.GpuFrame;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.BufferRenderer;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.VertexFormat;
import net.minecraft.client.render.VertexFormats;
import org.joml.Matrix4f;

/**
 * Draws the current frame over the whole window during HUD rendering.
 *
 * <p>The image is scaled uniformly to cover the window and the overflow is cropped by adjusting
 * the texture coordinates, so the picture is never stretched and no bars appear. It is not a
 * screen, so input and gameplay are not affected.</p>
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
	public void render(DrawContext context) {
		GpuFrame frame = controller.currentFrame();
		if (frame == null) {
			return;
		}
		int windowWidth = context.getScaledWindowWidth();
		int windowHeight = context.getScaledWindowHeight();
		if (windowWidth <= 0 || windowHeight <= 0 || frame.width() <= 0 || frame.height() <= 0) {
			return;
		}

		// "Cover" scaling: the scale that fills both axes, then crop the excess symmetrically.
		float scale = Math.max((float) windowWidth / frame.width(), (float) windowHeight / frame.height());
		float visibleU = windowWidth / (frame.width() * scale);
		float visibleV = windowHeight / (frame.height() * scale);
		float u0 = (1.0F - visibleU) / 2.0F;
		float u1 = u0 + visibleU;
		float v0 = (1.0F - visibleV) / 2.0F;
		float v1 = v0 + visibleV;

		Matrix4f matrix = context.getMatrices().peek().getPositionMatrix();

		RenderSystem.disableDepthTest();
		RenderSystem.enableBlend();
		RenderSystem.defaultBlendFunc();
		RenderSystem.setShader(GameRenderer::getPositionTexProgram);
		RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
		RenderSystem.setShaderTexture(0, frame.glId());

		BufferBuilder buffer = Tessellator.getInstance().begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_TEXTURE);
		buffer.vertex(matrix, 0.0F, 0.0F, 0.0F).texture(u0, v0);
		buffer.vertex(matrix, 0.0F, windowHeight, 0.0F).texture(u0, v1);
		buffer.vertex(matrix, windowWidth, windowHeight, 0.0F).texture(u1, v1);
		buffer.vertex(matrix, windowWidth, 0.0F, 0.0F).texture(u1, v0);
		BufferRenderer.drawWithGlobalProgram(buffer.end());

		RenderSystem.disableBlend();
		RenderSystem.enableDepthTest();
	}
}
