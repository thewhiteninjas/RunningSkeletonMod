package com.runningskeleton.mod;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderGuiOverlayEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.Random;

@Mod.EventBusSubscriber(modid = RunningSkeletonMod.MOD_ID, value = Dist.CLIENT)
public class SkeletonEventHandler {

    private static final Random RANDOM = new Random();
    private static long lastCheckTime = 0L;

    // Prebuilt ResourceLocations for all 14 frames
    private static final ResourceLocation[] FRAMES = new ResourceLocation[SkeletonConfig.TOTAL_FRAMES];

    static {
        for (int i = 0; i < SkeletonConfig.TOTAL_FRAMES; i++) {
            // Files: frame_01.png ... frame_14.png
            String name = String.format("textures/frames/frame_%02d.png", i + 1);
            FRAMES[i] = new ResourceLocation(RunningSkeletonMod.MOD_ID, name);
        }
    }

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;

        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null || mc.player == null) return;

        long now = System.currentTimeMillis();

        if (SkeletonAnimationState.isPlaying()) {
            // Advance animation frame
            SkeletonAnimationState.tickFrame();
        } else if (!SkeletonAnimationState.isInCooldown()) {
            // Check once per second (roughly 20 ticks = ~1000ms)
            if (now - lastCheckTime >= 1000L) {
                lastCheckTime = now;
                int roll = RANDOM.nextInt(SkeletonConfig.chance);
                if (roll == 0) {
                    SkeletonAnimationState.startAnimation();
                }
            }
        }
    }

    @SubscribeEvent
    public static void onRenderGuiOverlay(RenderGuiOverlayEvent.Post event) {
        if (!SkeletonAnimationState.isPlaying()) return;

        Minecraft mc = Minecraft.getInstance();
        int screenWidth = mc.getWindow().getGuiScaledWidth();
        int screenHeight = mc.getWindow().getGuiScaledHeight();

        int frameIndex = SkeletonAnimationState.getCurrentFrame();
        if (frameIndex < 0 || frameIndex >= SkeletonConfig.TOTAL_FRAMES) return;

        ResourceLocation texture = FRAMES[frameIndex];

        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.setShaderTexture(0, texture);
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();

        PoseStack poseStack = event.getGuiGraphics().pose();
        poseStack.pushPose();

        // Draw full-screen quad using Tesselator
        Tesselator tesselator = Tesselator.getInstance();
        BufferBuilder buffer = tesselator.getBuilder();
        buffer.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX);
        buffer.vertex(0,           screenHeight, 0).uv(0, 1).endVertex();
        buffer.vertex(screenWidth, screenHeight, 0).uv(1, 1).endVertex();
        buffer.vertex(screenWidth, 0,            0).uv(1, 0).endVertex();
        buffer.vertex(0,           0,            0).uv(0, 0).endVertex();
        tesselator.end();

        poseStack.popPose();

        RenderSystem.disableBlend();
    }
}
