package com.thewhiteninja.runningskeleton.client;

import com.thewhiteninja.runningskeleton.SkeletonConfig;
import net.minecraft.client.Minecraft;
import org.jetbrains.annotations.Nullable;

import java.util.Locale;
import java.util.concurrent.ThreadLocalRandom;

/**
 * State machine of the animation: IDLE, LOADING, PLAYING and COOLDOWN.
 *
 * <p>All timing uses {@link System#nanoTime()}. Every method must be called from the render
 * thread, which is also where the HUD layer is drawn.</p>
 */
public final class AnimationController {

    /** The phases of the animation. */
    public enum State {
        IDLE, LOADING, PLAYING, COOLDOWN;

        /**
         * Returns the lowercase name used in chat output.
         *
         * @return the lowercase state name
         */
        public String label() {
            return name().toLowerCase(Locale.ROOT);
        }
    }

    /** Playback speed in frames per second. */
    public static final int FRAMES_PER_SECOND = 30;

    /** Fixed cooldown after an animation ends, in seconds. */
    public static final int COOLDOWN_SECONDS = 5;

    private static final long NANOS_PER_SECOND = 1_000_000_000L;
    private static final AnimationController INSTANCE = new AnimationController();

    private final FrameManager frames = new FrameManager();

    private State state = State.IDLE;
    private boolean rollScheduled;
    private long nextRollNanos;
    private long playbackStartNanos;
    private long cooldownEndNanos;

    private AnimationController() {
    }

    /**
     * Returns the single controller instance.
     *
     * @return the controller
     */
    public static AnimationController get() {
        return INSTANCE;
    }

    /**
     * Advances the state machine. Called once per client tick.
     */
    public void tick() {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player == null || minecraft.level == null) {
            reset();
            return;
        }
        long now = System.nanoTime();
        switch (state) {
            case IDLE -> tickIdle(minecraft, now);
            case LOADING -> tickLoading(now);
            case PLAYING -> tickPlaying(now);
            case COOLDOWN -> tickCooldown(now);
        }
    }

    /**
     * Starts the animation immediately, ignoring the random chance and the cooldown.
     *
     * @return false if the animation is already loading or playing, true if it was started
     */
    public boolean show() {
        if (state == State.LOADING || state == State.PLAYING) {
            return false;
        }
        begin();
        return true;
    }

    /**
     * Stops everything and frees all resources, returning to IDLE. Used when leaving the world.
     */
    public void reset() {
        frames.release();
        state = State.IDLE;
        rollScheduled = false;
    }

    /**
     * Returns the current state.
     *
     * @return the state
     */
    public State getState() {
        return state;
    }

    /**
     * Describes the state for chat output, including the remaining seconds during the cooldown.
     *
     * @return a short description such as {@code cooldown (3 s restantes)}
     */
    public String describeState() {
        if (state == State.COOLDOWN) {
            return state.label() + " (" + cooldownRemainingSeconds(System.nanoTime()) + " s restantes)";
        }
        return state.label();
    }

    /**
     * Returns the texture of the frame that must be drawn right now.
     *
     * @return the texture and its pixel size, or null if nothing should be drawn
     */
    @Nullable
    public FrameManager.FrameView currentFrame() {
        if (state != State.PLAYING) {
            return null;
        }
        int index = Math.min(frameIndexAt(System.nanoTime()), FrameManager.FRAME_COUNT - 1);
        return frames.frameFor(index);
    }

    private void tickIdle(Minecraft minecraft, long now) {
        if (minecraft.isPaused()) {
            return;
        }
        if (!rollScheduled) {
            rollScheduled = true;
            nextRollNanos = now + NANOS_PER_SECOND;
            return;
        }
        if (now - nextRollNanos < 0) {
            return;
        }
        nextRollNanos = now + NANOS_PER_SECOND;
        if (ThreadLocalRandom.current().nextInt(SkeletonConfig.getChance()) == 0) {
            begin();
        }
    }

    private void tickLoading(long now) {
        frames.update(0);
        if (frames.hasFailed()) {
            enterCooldown(now);
        } else if (frames.contiguousReady() >= FrameManager.START_BUFFER) {
            state = State.PLAYING;
            playbackStartNanos = now;
        }
    }

    private void tickPlaying(long now) {
        int index = frameIndexAt(now);
        if (index >= FrameManager.FRAME_COUNT) {
            enterCooldown(now);
            return;
        }
        frames.update(index);
        if (frames.hasFailed()) {
            enterCooldown(now);
        }
    }

    private void tickCooldown(long now) {
        if (now - cooldownEndNanos >= 0) {
            state = State.IDLE;
            rollScheduled = false;
        }
    }

    private void begin() {
        frames.start();
        state = State.LOADING;
    }

    private void enterCooldown(long now) {
        frames.release();
        state = State.COOLDOWN;
        cooldownEndNanos = now + COOLDOWN_SECONDS * NANOS_PER_SECOND;
    }

    private int frameIndexAt(long now) {
        long elapsed = Math.max(0L, now - playbackStartNanos);
        return (int) (elapsed * FRAMES_PER_SECOND / NANOS_PER_SECOND);
    }

    private long cooldownRemainingSeconds(long now) {
        long remaining = cooldownEndNanos - now;
        if (remaining <= 0) {
            return 0;
        }
        return (remaining + NANOS_PER_SECOND - 1) / NANOS_PER_SECOND;
    }
}
