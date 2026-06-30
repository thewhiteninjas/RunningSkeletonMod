package com.thewhiteninja.runningskeleton.client;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.RenderPipelines;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.util.Identifier;

/**
 * Handles playback and rendering of the fullscreen jumpscare-style animation.
 * <p>
 * The animation consists of 14 PNG frames (frame_01.png .. frame_14.png),
 * each 1024x1024 pixels, located under the mod's texture folder. When
 * playing, one frame is shown per game tick advance based on
 * {@link #TICKS_PER_FRAME}, and the image is scaled to always cover the
 * full game window regardless of resolution.
 */
public final class AnimationHandler {

    /** Total number of frames in the animation. */
    public static final int FRAME_COUNT = 14;

    /** How many client ticks each frame is displayed for. */
    public static final int TICKS_PER_FRAME = 2;

    private static final String NAMESPACE = "runningskeleton";

    private static final Identifier[] FRAME_TEXTURES = new Identifier[FRAME_COUNT];

    static {
        for (int i = 0; i < FRAME_COUNT; i++) {
            String fileName = String.format("textures/animation/frame_%02d.png", i + 1);
            FRAME_TEXTURES[i] = Identifier.of(NAMESPACE, fileName);
        }
    }

    private static boolean playing = false;
    private static int currentFrame = 0;
    private static int ticksOnCurrentFrame = 0;

    private AnimationHandler() {
        // Utility class, no instances.
    }

    public static boolean isPlaying() {
        return playing;
    }

    /**
     * Starts the animation from the first frame. If an animation is already
     * playing, it is restarted from the beginning.
     */
    public static void start() {
        playing = true;
        currentFrame = 0;
        ticksOnCurrentFrame = 0;
    }

    /**
     * Advances the animation by one client tick. Should be called once per
     * client tick while {@link #isPlaying()} is true.
     */
    public static void tick() {
        if (!playing) {
            return;
        }

        ticksOnCurrentFrame++;
        if (ticksOnCurrentFrame >= TICKS_PER_FRAME) {
            ticksOnCurrentFrame = 0;
            currentFrame++;

            if (currentFrame >= FRAME_COUNT) {
                // Animation finished playing all frames.
                playing = false;
                currentFrame = 0;
                SkeletonState.startCooldown();
            }
        }
    }

    /**
     * Renders the current animation frame, scaled to fill the entire game
     * window. Does nothing if the animation is not currently playing.
     *
     * @param context the current draw context, supplied by HudRenderCallback
     */
    public static void render(DrawContext context) {
        if (!playing) return;

        MinecraftClient client = MinecraftClient.getInstance();

        int w = client.getWindow().getScaledWidth();
        int h = client.getWindow().getScaledHeight();

        Identifier texture = FRAME_TEXTURES[currentFrame];

        context.drawTexture(
                RenderPipelines.GUI_TEXTURED,
                texture,
                0,
                0,
                0.0F,
                0.0F,
                w,
                h,
                w,
                h
        );
    }
}