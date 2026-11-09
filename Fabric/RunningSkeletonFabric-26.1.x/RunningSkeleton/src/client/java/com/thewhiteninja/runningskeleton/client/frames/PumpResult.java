package com.thewhiteninja.runningskeleton.client.frames;

/**
 * Outcome of one {@link FrameTextureManager#pump(int)} call.
 *
 * @param status   what happened
 * @param fileName the frame file involved (for {@code MISSING} and {@code FAILED})
 * @param error    the decode/upload error (for {@code FAILED})
 */
public record PumpResult(Status status, String fileName, Throwable error) {

	/** Possible outcomes. */
	public enum Status {
		OK, MISSING, FAILED
	}

	/** Nothing went wrong. */
	public static final PumpResult SUCCESS = new PumpResult(Status.OK, null, null);

	static PumpResult missing(String fileName) {
		return new PumpResult(Status.MISSING, fileName, null);
	}

	static PumpResult failed(String fileName, Throwable error) {
		return new PumpResult(Status.FAILED, fileName, error);
	}
}
