package com.thewhiteninja.runningskeleton.client.animation;

import com.thewhiteninja.runningskeleton.client.config.ModConfig;
import net.minecraft.client.Minecraft;

import java.util.concurrent.ThreadLocalRandom;

/**
 * Tracks the state of the running skeleton animation: the once-per-second probability
 * check, frame playback timing and the post-animation cooldown.
 * All timing is wall-clock based (System.currentTimeMillis()) so it never blocks or
 * depends on the render thread; the client tick only advances the state machine.
 */
public final class AnimationManager {

	private static final int FRAME_COUNT = 14;
	private static final long FRAME_DURATION_MILLIS = 80L;
	private static final long ANIMATION_DURATION_MILLIS = FRAME_COUNT * FRAME_DURATION_MILLIS;
	private static final long COOLDOWN_DURATION_MILLIS = 5000L;
	private static final long PROBABILITY_CHECK_INTERVAL_MILLIS = 1000L;

	private volatile int chanceDenominator = ModConfig.DEFAULT_CHANCE_DENOMINATOR;
	private long animationStartMillis = -1L;
	private long cooldownEndMillis = 0L;
	private long lastProbabilityCheckMillis = 0L;

	public void onClientTick(Minecraft client) {
		if (client.player == null || client.level == null) {
			return;
		}

		long now = System.currentTimeMillis();

		if (isAnimating(now)) {
			return;
		}

		if (animationStartMillis != -1L) {
			animationStartMillis = -1L;
			cooldownEndMillis = now + COOLDOWN_DURATION_MILLIS;
		}

		if (now < cooldownEndMillis) {
			return;
		}

		if (now - lastProbabilityCheckMillis < PROBABILITY_CHECK_INTERVAL_MILLIS) {
			return;
		}

		lastProbabilityCheckMillis = now;

		if (ThreadLocalRandom.current().nextInt(chanceDenominator) == 0) {
			animationStartMillis = now;
		}
	}

	public void forceShow() {
		animationStartMillis = System.currentTimeMillis();
	}

	/**
	 * @return the 0-based index of the frame that should currently be drawn,
	 * or -1 if no frame should be drawn.
	 */
	public int getCurrentFrameIndex() {
		if (animationStartMillis == -1L) {
			return -1;
		}

		long elapsed = System.currentTimeMillis() - animationStartMillis;
		int frameIndex = (int) (elapsed / FRAME_DURATION_MILLIS);
		return frameIndex < FRAME_COUNT ? frameIndex : -1;
	}

	public void setChanceDenominator(int denominator) {
		this.chanceDenominator = denominator;
	}

	public int getChanceDenominator() {
		return chanceDenominator;
	}

	public boolean isCurrentlyAnimating() {
		return isAnimating(System.currentTimeMillis());
	}

	public boolean isInCooldown() {
		long now = System.currentTimeMillis();
		return !isAnimating(now) && now < cooldownEndMillis;
	}

	public long getCooldownRemainingMillis() {
		return Math.max(cooldownEndMillis - System.currentTimeMillis(), 0L);
	}

	private boolean isAnimating(long now) {
		return animationStartMillis != -1L && (now - animationStartMillis) < ANIMATION_DURATION_MILLIS;
	}
}
