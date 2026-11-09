package com.thewhiteninja.runningskeleton.client.animation;

import com.thewhiteninja.runningskeleton.RunningSkeletonMod;
import com.thewhiteninja.runningskeleton.client.config.ConfigManager;
import com.thewhiteninja.runningskeleton.client.frames.FrameTextureManager;
import com.thewhiteninja.runningskeleton.client.frames.GpuFrame;
import com.thewhiteninja.runningskeleton.client.frames.PumpResult;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.ChatFormatting;

import java.util.concurrent.ThreadLocalRandom;

/**
 * State machine driving the animation: {@code IDLE -> LOADING -> PLAYING -> COOLDOWN -> IDLE}.
 *
 * <p>Everything here runs on the client thread. Timing uses {@link System#nanoTime()}.</p>
 */
public final class AnimationController {

	/** Playback speed in frames per second. */
	public static final int FPS = 30;
	/** Fixed cooldown after an animation ends or fails, in seconds. */
	public static final int COOLDOWN_SECONDS = 5;
	/** Number of leading frames that must be uploaded before playback starts. */
	public static final int FRAMES_BEFORE_START = 8;

	private static final long NANOS_PER_SECOND = 1_000_000_000L;
	private static final long COOLDOWN_NANOS = COOLDOWN_SECONDS * NANOS_PER_SECOND;

	private final ConfigManager config;
	private final FrameTextureManager frames = new FrameTextureManager();

	private AnimationState state = AnimationState.IDLE;
	private long lastRollNanos = System.nanoTime();
	private long cooldownEndNanos;
	private long playbackStartNanos;

	/**
	 * Creates the controller.
	 *
	 * @param config the configuration holding the probability
	 */
	public AnimationController(ConfigManager config) {
		this.config = config;
	}

	/** @return the current state */
	public AnimationState getState() {
		return state;
	}

	/** @return the remaining cooldown in milliseconds, or 0 if not cooling down */
	public long getCooldownRemainingMillis() {
		if (state != AnimationState.COOLDOWN) {
			return 0L;
		}
		long remaining = cooldownEndNanos - System.nanoTime();
		return remaining <= 0L ? 0L : remaining / 1_000_000L;
	}

	/**
	 * Called once per client tick: random trigger, cooldown expiry, loading and playback progress.
	 *
	 * @param client the Minecraft client
	 */
	public void tick(Minecraft client) {
		long now = System.nanoTime();
		switch (state) {
			case IDLE:
				if (client.level == null || client.player == null) {
					lastRollNanos = now;
					return;
				}
				if (now - lastRollNanos >= NANOS_PER_SECOND) {
					lastRollNanos = now;
					if (ThreadLocalRandom.current().nextInt(config.getChance()) == 0) {
						begin(client);
					}
				}
				break;
			case COOLDOWN:
				if (now >= cooldownEndNanos) {
					state = AnimationState.IDLE;
					lastRollNanos = now;
				}
				break;
			case LOADING:
				if (handle(client, frames.pump(0))) {
					return;
				}
				if (frames.contiguousReady() >= FRAMES_BEFORE_START) {
					playbackStartNanos = now;
					state = AnimationState.PLAYING;
				}
				break;
			case PLAYING:
				int index = rawFrameIndex(now);
				if (index >= FrameTextureManager.TOTAL_FRAMES) {
					enterCooldown();
					return;
				}
				handle(client, frames.pump(index));
				break;
			default:
				break;
		}
	}

	/**
	 * Starts the animation immediately, ignoring probability, cooldown and the current state.
	 *
	 * @param client the Minecraft client
	 */
	public void forceStart(Minecraft client) {
		begin(client);
	}

	/**
	 * Returns the frame that should be drawn right now.
	 *
	 * @return the most recent available frame, or {@code null} if nothing should be drawn
	 */
	public GpuFrame currentFrame() {
		if (state != AnimationState.PLAYING) {
			return null;
		}
		int index = Math.min(rawFrameIndex(System.nanoTime()), FrameTextureManager.TOTAL_FRAMES - 1);
		return frames.frameFor(index);
	}

	/** Called when the player leaves the world: frees all resources and returns to idle. */
	public void onDisconnect() {
		frames.release();
		state = AnimationState.IDLE;
		lastRollNanos = System.nanoTime();
	}

	/** Called when the game is closing: frees all resources and stops the decoder threads. */
	public void shutdown() {
		frames.shutdown();
		state = AnimationState.IDLE;
	}

	private void begin(Minecraft client) {
		frames.begin(client.getResourceManager());
		state = AnimationState.LOADING;
	}

	private int rawFrameIndex(long now) {
		return (int) Math.min(Integer.MAX_VALUE, ((now - playbackStartNanos) * FPS) / NANOS_PER_SECOND);
	}

	private void enterCooldown() {
		frames.release();
		state = AnimationState.COOLDOWN;
		cooldownEndNanos = System.nanoTime() + COOLDOWN_NANOS;
	}

	/**
	 * Reacts to a pump result.
	 *
	 * @return {@code true} if the animation was aborted
	 */
	private boolean handle(Minecraft client, PumpResult result) {
		switch (result.status()) {
			case MISSING:
				RunningSkeletonMod.LOGGER.warn("Missing animation frame: {}", result.fileName());
				client.gui.getChat().addMessage(
						Component.literal("[RunningSkeleton] Missing animation frame: " + result.fileName())
								.withStyle(ChatFormatting.RED));
				enterCooldown();
				return true;
			case FAILED:
				RunningSkeletonMod.LOGGER.error("Failed to load animation frame {}", result.fileName(), result.error());
				enterCooldown();
				return true;
			default:
				return false;
		}
	}
}
