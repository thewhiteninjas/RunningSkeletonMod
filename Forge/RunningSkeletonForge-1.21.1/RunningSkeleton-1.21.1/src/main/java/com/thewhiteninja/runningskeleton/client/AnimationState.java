package com.thewhiteninja.runningskeleton.client;

/** Lifecycle states of the skeleton animation. */
public enum AnimationState {
    /** Nothing is happening; the random roll runs every second. */
    IDLE("idle"),
    /** Frames are being loaded into GPU memory before playback starts. */
    LOADING("loading"),
    /** The animation is being drawn on screen. */
    PLAYING("playing"),
    /** The animation has just ended; automatic spawns are blocked. */
    COOLDOWN("cooldown");

    private final String displayName;

    AnimationState(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
