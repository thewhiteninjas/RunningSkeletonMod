package com.thewhiteninja.runningskeleton.client.frames;

import com.mojang.blaze3d.platform.TextureUtil;

/**
 * A frame already uploaded to the GPU.
 *
 * <p>Must only be created and closed on the render thread.</p>
 */
public final class GpuFrame {

	private final int glId;
	private final int width;
	private final int height;
	private boolean closed;

	GpuFrame(int glId, int width, int height) {
		this.glId = glId;
		this.width = width;
		this.height = height;
	}

	/** @return the OpenGL texture id */
	public int glId() {
		return glId;
	}

	/** @return the real width of the source PNG in pixels */
	public int width() {
		return width;
	}

	/** @return the real height of the source PNG in pixels */
	public int height() {
		return height;
	}

	/** Deletes the GPU texture. Safe to call more than once. */
	void close() {
		if (!closed) {
			closed = true;
			TextureUtil.releaseTextureId(glId);
		}
	}
}
