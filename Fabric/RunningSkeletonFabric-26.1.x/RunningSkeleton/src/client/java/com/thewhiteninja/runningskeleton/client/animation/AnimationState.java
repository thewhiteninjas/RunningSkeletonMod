package com.thewhiteninja.runningskeleton.client.animation;

/** States of the animation state machine. */
public enum AnimationState {
	/** Waiting for the next random roll. */
	IDLE("idle"),
	/** Decoding and uploading the first frames. */
	LOADING("loading"),
	/** Frames are being displayed. */
	PLAYING("playing"),
	/** Automatic triggering is blocked for a fixed period. */
	COOLDOWN("cooldown");

	private final String label;

	AnimationState(String label) {
		this.label = label;
	}

	/**
	 * Returns the lowercase label shown to the player.
	 *
	 * @return the label
	 */
	public String label() {
		return label;
	}
}
