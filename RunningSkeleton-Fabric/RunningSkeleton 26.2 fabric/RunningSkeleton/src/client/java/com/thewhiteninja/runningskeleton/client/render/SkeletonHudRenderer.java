package com.thewhiteninja.runningskeleton.client.render;

import com.mojang.blaze3d.platform.Window;
import com.thewhiteninja.runningskeleton.client.RunningSkeletonClient;
import com.thewhiteninja.runningskeleton.client.animation.AnimationManager;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.resources.Identifier;

/**
 * Draws the current animation frame, stretched to cover the whole game window
 * regardless of its current size or aspect ratio.
 */
public final class SkeletonHudRenderer {

	private static final int FRAME_COUNT = 14;
	private static final int FRAME_SIZE = 1024;
	private static final Identifier[] FRAME_TEXTURES = buildFrameTextures();

	private final AnimationManager animationManager;

	public SkeletonHudRenderer(AnimationManager animationManager) {
		this.animationManager = animationManager;
	}

	private static Identifier[] buildFrameTextures() {
		Identifier[] frames = new Identifier[FRAME_COUNT];
		for (int i = 0; i < FRAME_COUNT; i++) {
			String path = String.format("textures/gui/skeleton/frame_%02d.png", i + 1);
			frames[i] = Identifier.fromNamespaceAndPath(RunningSkeletonClient.MOD_ID, path);
		}
		return frames;
	}

	public void render(GuiGraphicsExtractor graphics, DeltaTracker deltaTracker) {
		int frameIndex = animationManager.getCurrentFrameIndex();
		if (frameIndex < 0) {
			return;
		}

		Window window = Minecraft.getInstance().getWindow();
		int screenWidth = window.getGuiScaledWidth();
		int screenHeight = window.getGuiScaledHeight();

		graphics.blit(
				RenderPipelines.GUI_TEXTURED,
				FRAME_TEXTURES[frameIndex],
				0, 0,
				0, 0,
				screenWidth, screenHeight,
				FRAME_SIZE, FRAME_SIZE,
				FRAME_SIZE, FRAME_SIZE
		);
	}
}
