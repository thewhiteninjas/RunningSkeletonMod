package com.thewhiteninja.elephantgreenscreeneffect;

import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.logging.LogUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.resources.ResourceLocation;
import org.slf4j.Logger;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Drives the elephant green-screen animation: rolls a once-per-second chance
 * to trigger while idle, then renders the frame sequence full-screen over
 * the HUD until it finishes. Ported from the Forge {@code TickEvent.ClientTickEvent}
 * / {@code RenderGuiEvent.Post} listeners to Fabric API's
 * {@code ClientTickEvents.END_CLIENT_TICK} / {@code HudRenderCallback}, with
 * identical timing, cooldown and rendering behaviour.
 */
public final class ElephantAnimationManager {

    private static final int TOTAL_FRAMES = 71;
    private static final int FRAME_WIDTH = 398;
    private static final int FRAME_HEIGHT = 281;
    private static final int TICKS_PER_SECOND = 20;
    private static final int COOLDOWN_TICKS = 5 * TICKS_PER_SECOND;
    private static final long ANIMATION_DURATION_MILLIS = 1800L;
    private static final double FRAME_DURATION_MILLIS = (double) ANIMATION_DURATION_MILLIS / TOTAL_FRAMES;
    private static final String TEXTURE_PATH_FORMAT =
            "assets/" + ElephantGreenScreenEffectClient.MOD_ID + "/textures/elephant/frame_(%d).png";
    private static final String SOUND_PATH =
            "assets/" + ElephantGreenScreenEffectClient.MOD_ID + "/sounds/elephant.ogg";

    private static final Logger LOGGER = LogUtils.getLogger();
    private static final Random RANDOM = new Random();

    private static List<ResourceLocation> frameTextures;
    private static boolean texturesLoadAttempted = false;
    private static boolean texturesAvailable = false;

    private static boolean playing = false;
    private static int currentFrame = 0;
    private static long animationStartTimeMillis = 0L;

    private static int secondTickCounter = 0;
    private static int cooldownTicksRemaining = 0;

    private ElephantAnimationManager() {
    }

    public static void onClientTick(Minecraft minecraft) {
        if (minecraft.level == null) {
            return;
        }

        if (cooldownTicksRemaining > 0) {
            cooldownTicksRemaining--;
        }

        if (playing) {
            return;
        }

        secondTickCounter++;
        if (secondTickCounter >= TICKS_PER_SECOND) {
            secondTickCounter = 0;
            rollChance();
        }
    }

    private static void rollChance() {
        if (cooldownTicksRemaining > 0) {
            return;
        }

        int chance = ClientConfig.getChance();
        if (chance <= 0) {
            return;
        }

        if (RANDOM.nextInt(chance) == 0) {
            startAnimation();
        }
    }


    public static boolean startAnimation() {
        if (playing) {
            LOGGER.info("[ElephantGreenScreenEffect] startAnimation() ignored: animation already playing.");
            return false;
        }

        if (!ensureTexturesLoaded()) {
            LOGGER.warn("[ElephantGreenScreenEffect] Cannot start animation: frame textures are missing or invalid. "
                    + "Check that all 71 files exist at "
                    + "src/main/resources/assets/" + ElephantGreenScreenEffectClient.MOD_ID
                    + "/textures/elephant/frame_(1..48).png and that the project was rebuilt/resynced after adding them.");
            return false;
        }

        if (!isSoundPresent()) {
            LOGGER.warn("[ElephantGreenScreenEffect] elephant.ogg was not found at "
                    + "src/main/resources/assets/" + ElephantGreenScreenEffectClient.MOD_ID
                    + "/sounds/elephant.ogg. The animation will still play, but without sound.");
        }

        playing = true;
        currentFrame = 0;
        animationStartTimeMillis = System.currentTimeMillis();

        Minecraft.getInstance().getSoundManager().play(
                SimpleSoundInstance.forUI(ModSounds.ELEPHANT_SOUND, 1.0F));

        return true;
    }

    private static boolean isSoundPresent() {
        try (InputStream stream = ElephantAnimationManager.class.getClassLoader().getResourceAsStream(SOUND_PATH)) {
            return stream != null;
        } catch (IOException exception) {
            return false;
        }
    }

    public static void onRenderGui(GuiGraphics guiGraphics, float tickDelta) {
        if (!playing) {
            return;
        }

        if (!ensureTexturesLoaded()) {
            playing = false;
            return;
        }

        long elapsedMillis = System.currentTimeMillis() - animationStartTimeMillis;

        if (elapsedMillis >= ANIMATION_DURATION_MILLIS) {
            playing = false;
            currentFrame = 0;
            cooldownTicksRemaining = COOLDOWN_TICKS;
            return;
        }

        currentFrame = (int) Math.min(TOTAL_FRAMES - 1, elapsedMillis / FRAME_DURATION_MILLIS);
        ResourceLocation texture = frameTextures.get(currentFrame);

        int screenWidth = Minecraft.getInstance().getWindow().getGuiScaledWidth();
        int screenHeight = Minecraft.getInstance().getWindow().getGuiScaledHeight();

        // Stretch the frame to the full window resolution, ignoring aspect ratio.
        guiGraphics.blit(
                texture,
                0, 0,
                screenWidth, screenHeight,
                0.0F, 0.0F,
                FRAME_WIDTH, FRAME_HEIGHT,
                FRAME_WIDTH, FRAME_HEIGHT
        );
    }

    /**
     * Lazily loads and registers the 71 dynamic textures on first use.
     * Returns true if the loading process ran (regardless of outcome);
     * check {@link #texturesAvailable} for the actual result.
     */
    private static boolean ensureTexturesLoaded() {
        if (texturesLoadAttempted) {
            return texturesAvailable;
        }

        texturesLoadAttempted = true;
        frameTextures = new ArrayList<>(TOTAL_FRAMES);

        Minecraft minecraft = Minecraft.getInstance();
        boolean allLoaded = true;

        for (int i = 1; i <= TOTAL_FRAMES; i++) {
            String path = String.format(TEXTURE_PATH_FORMAT, i);

            try (InputStream stream = ElephantAnimationManager.class.getClassLoader().getResourceAsStream(path)) {
                if (stream == null) {
                    LOGGER.warn("[ElephantGreenScreenEffect] Missing texture on classpath: {}", path);
                    allLoaded = false;
                    continue;
                }

                NativeImage nativeImage = NativeImage.read(stream);
                DynamicTexture dynamicTexture = new DynamicTexture(nativeImage);
                ResourceLocation location = new ResourceLocation(
                        ElephantGreenScreenEffectClient.MOD_ID, "dynamic/frame_" + i);

                minecraft.getTextureManager().register(location, dynamicTexture);
                frameTextures.add(location);
            } catch (IOException exception) {
                LOGGER.warn("[ElephantGreenScreenEffect] Failed to read texture: {}", path, exception);
                allLoaded = false;
            }
        }

        texturesAvailable = allLoaded && frameTextures.size() == TOTAL_FRAMES;

        if (texturesAvailable) {
            LOGGER.info("[ElephantGreenScreenEffect] Successfully loaded all {} animation frames.", TOTAL_FRAMES);
        }

        return texturesAvailable;
    }
}
