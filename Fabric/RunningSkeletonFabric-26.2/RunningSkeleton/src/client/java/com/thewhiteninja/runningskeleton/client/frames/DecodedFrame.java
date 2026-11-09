package com.thewhiteninja.runningskeleton.client.frames;

import com.mojang.blaze3d.platform.NativeImage;

/**
 * Result of decoding one PNG on a background thread.
 *
 * <p>Owns the {@link NativeImage} until it is handed over with {@link #takeImage()} or released
 * with {@link #close()}.</p>
 */
final class DecodedFrame {

	/** Outcome of a decode task. */
	enum Kind {
		OK, MISSING, FAILED, SKIPPED
	}

	private final Kind kind;
	private final int index;
	private final String fileName;
	private final Throwable error;
	private NativeImage image;

	private DecodedFrame(Kind kind, int index, String fileName, NativeImage image, Throwable error) {
		this.kind = kind;
		this.index = index;
		this.fileName = fileName;
		this.image = image;
		this.error = error;
	}

	static DecodedFrame ok(int index, String fileName, NativeImage image) {
		return new DecodedFrame(Kind.OK, index, fileName, image, null);
	}

	static DecodedFrame missing(int index, String fileName) {
		return new DecodedFrame(Kind.MISSING, index, fileName, null, null);
	}

	static DecodedFrame failed(int index, String fileName, Throwable error) {
		return new DecodedFrame(Kind.FAILED, index, fileName, null, error);
	}

	static DecodedFrame skipped(int index, String fileName) {
		return new DecodedFrame(Kind.SKIPPED, index, fileName, null, null);
	}

	Kind kind() {
		return kind;
	}

	int index() {
		return index;
	}

	String fileName() {
		return fileName;
	}

	Throwable error() {
		return error;
	}

	/** Transfers ownership of the image to the caller (who must close it). */
	synchronized NativeImage takeImage() {
		NativeImage taken = image;
		image = null;
		return taken;
	}

	/** Closes the image if it is still owned. Safe to call from any thread and more than once. */
	synchronized void close() {
		if (image != null) {
			image.close();
			image = null;
		}
	}
}
