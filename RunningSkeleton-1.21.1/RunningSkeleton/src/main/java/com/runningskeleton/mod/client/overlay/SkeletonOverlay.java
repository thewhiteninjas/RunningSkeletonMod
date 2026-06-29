package com.runningskeleton.mod.client.overlay;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.LayeredDraw;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.resources.ResourceLocation;
import com.runningskeleton.mod.RunningSkeleton;

import java.util.Random;

public class SkeletonOverlay implements LayeredDraw.Layer {

    public static final SkeletonOverlay INSTANCE = new SkeletonOverlay();

    // Probability: default 1 in 2000 per second check (configurable via command)
    // We check every tick (20 ticks/sec), so per-tick chance = chanceOneIn / 20
    // To get 1/2000 per second, per-tick chance denominator = 2000 * 20 = 40000
    // But we track seconds: we use a secondTimer to fire once per second
    private static int chanceOneIn = 2000; // 1 in X chance per second

    // Animation state
    private static final int TOTAL_FRAMES = 14;
    // Each frame duration in milliseconds - adjust for desired GIF speed
    // For a smooth ~15fps animation: 1000ms / 14 frames ≈ 71ms per frame
    private static final long FRAME_DURATION_MS = 71L;
    // Cooldown after animation ends (milliseconds)
    private static final long COOLDOWN_MS = 5000L;

    private boolean isPlaying = false;
    private int currentFrame = 0;
    private long frameStartTime = 0L;
    private long cooldownEndTime = 0L;

    // Second accumulator to fire once per second
    private long lastSecondCheck = 0L;

    // Precomputed ResourceLocations for all 14 frames
    private static final ResourceLocation[] FRAMES = new ResourceLocation[TOTAL_FRAMES];

    static {
        for (int i = 0; i < TOTAL_FRAMES; i++) {
            // frame_01.png ... frame_14.png
            String name = String.format("textures/overlay/frame_%02d.png", i + 1);
            FRAMES[i] = ResourceLocation.fromNamespaceAndPath(RunningSkeleton.MOD_ID, name);
        }
    }

    private final Random random = new Random();

    private SkeletonOverlay() {}

    // Called by command to change probability
    public static void setChance(int oneIn) {
        chanceOneIn = Math.max(1, oneIn);
    }

    public static int getChance() {
        return chanceOneIn;
    }

    @Override
    public void render(GuiGraphics guiGraphics, DeltaTracker deltaTracker) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) return;
        // Don't show during loading, in menus, etc.
        if (mc.screen != null) return;

        long now = System.currentTimeMillis();

        if (!isPlaying) {
            // Are we still in cooldown?
            if (now < cooldownEndTime) return;

            // Check once per second
            if (now - lastSecondCheck >= 1000L) {
                lastSecondCheck = now;
                // Roll 1 in chanceOneIn
                if (random.nextInt(chanceOneIn) == 0) {
                    startAnimation(now);
                }
            }
            return;
        }

        // --- Animation is playing ---
        // Advance frame
        long elapsed = now - frameStartTime;
        int targetFrame = (int) (elapsed / FRAME_DURATION_MS);

        if (targetFrame >= TOTAL_FRAMES) {
            // Animation finished
            stopAnimation(now);
            return;
        }

        currentFrame = targetFrame;

        // Render current frame fullscreen
        renderFrame(guiGraphics, mc);
    }

    private void startAnimation(long now) {
        isPlaying = true;
        currentFrame = 0;
        frameStartTime = now;
    }

    private void stopAnimation(long now) {
        isPlaying = false;
        currentFrame = 0;
        cooldownEndTime = now + COOLDOWN_MS;
        lastSecondCheck = now; // reset so we don't immediately re-check
    }

    private void renderFrame(GuiGraphics guiGraphics, Minecraft mc) {
        int screenWidth = mc.getWindow().getGuiScaledWidth();
        int screenHeight = mc.getWindow().getGuiScaledHeight();

        ResourceLocation texture = FRAMES[currentFrame];

        // 1.21.1 compatible rendering:
        // Set up the shader and texture via RenderSystem, then use GuiGraphics.blit
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderTexture(0, texture);
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);

        // blit(ResourceLocation, x, y, u, v, width, height, textureWidth, textureHeight)
        // We pass screenWidth/screenHeight as both render size and texture size so it stretches
        guiGraphics.blit(
                texture,
                0, 0,                       // Screen x, y (top-left corner)
                0, 0,                       // Texture u, v offset
                screenWidth, screenHeight,  // Render width, height (full screen)
                screenWidth, screenHeight   // Texture width, height (source dimensions)
        );

        RenderSystem.disableBlend();
        RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
    }

    /**
     * Force-trigger the animation (for testing / admin use).
     */
    public void triggerNow() {
        if (!isPlaying) {
            startAnimation(System.currentTimeMillis());
        }
    }
}