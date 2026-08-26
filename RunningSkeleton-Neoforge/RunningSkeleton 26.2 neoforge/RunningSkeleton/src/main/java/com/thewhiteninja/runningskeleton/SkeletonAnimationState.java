package com.thewhiteninja.runningskeleton;

import java.util.Random;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;

/**
 * Tracks the running skeleton animation state (idle, playing or on cooldown) and advances it
 * once per client tick. State is read by {@link SkeletonOverlayRenderer} to decide what, if
 * anything, to draw on screen this frame.
 */
@EventBusSubscriber(modid = RunningSkeletonMod.MOD_ID, value = Dist.CLIENT)
public final class SkeletonAnimationState {

    private static final int TICKS_PER_SECOND = 20;
    private static final long FRAME_DURATION_NANOS = 70_000_000L;
    private static final long COOLDOWN_NANOS = 5_000_000_000L;

    private static final Random RANDOM = new Random();

    private enum Phase {
        IDLE,
        PLAYING,
        COOLDOWN
    }

    private static Phase phase = Phase.IDLE;
    private static long animationStartNanos;
    private static long cooldownEndNanos;
    private static int secondTickCounter;

    private SkeletonAnimationState() {
    }

    @SubscribeEvent
    static void onClientTick(ClientTickEvent.Post event) {
        long now = System.nanoTime();

        if (phase == Phase.COOLDOWN && now >= cooldownEndNanos) {
            phase = Phase.IDLE;
        }

        if (phase == Phase.PLAYING && now - animationStartNanos >= animationDurationNanos()) {
            phase = Phase.COOLDOWN;
            cooldownEndNanos = now + COOLDOWN_NANOS;
        }

        if (phase != Phase.IDLE) {
            return;
        }

        secondTickCounter++;
        if (secondTickCounter < TICKS_PER_SECOND) {
            return;
        }
        secondTickCounter = 0;

        int chance = RunningSkeletonConfig.getChance();
        if (RANDOM.nextInt(chance) == 0) {
            start(now);
        }
    }

    /**
     * Forces the animation to play immediately, bypassing the probability roll and any
     * active cooldown. Used by {@code /skeleton show}.
     */
    static void forceStart() {
        start(System.nanoTime());
    }

    static boolean isPlaying() {
        return phase == Phase.PLAYING;
    }

    static boolean isOnCooldown() {
        return phase == Phase.COOLDOWN;
    }

    static int currentFrameIndex() {
        long elapsed = System.nanoTime() - animationStartNanos;
        int index = (int) (elapsed / FRAME_DURATION_NANOS);
        return Math.min(index, SkeletonTextures.FRAME_COUNT - 1);
    }

    static long remainingCooldownMillis() {
        if (phase != Phase.COOLDOWN) {
            return 0L;
        }
        return Math.max(0L, (cooldownEndNanos - System.nanoTime()) / 1_000_000L);
    }

    private static void start(long now) {
        phase = Phase.PLAYING;
        animationStartNanos = now;
        secondTickCounter = 0;
    }

    private static long animationDurationNanos() {
        return FRAME_DURATION_NANOS * SkeletonTextures.FRAME_COUNT;
    }
}
