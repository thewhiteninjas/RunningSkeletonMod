package com.thewhiteninja.runningskeleton.client.frames;

import net.minecraft.client.MinecraftClient;
import net.minecraft.util.Identifier;

/**
 * A frame already uploaded to the GPU and registered as a dynamic texture.
 *
 * <p>Must only be created and closed on the render thread.</p>
 */
public final class GpuFrame {

	private final Identifier id;
	private final int width;
	private final int height;
	private boolean closed;

	GpuFrame(Identifier id, int width, int height) {
		this.id = id;
		this.width = width;
		this.height = height;
	}

	/** @return the identifier the texture is registered under */
	public Identifier id() {
		return id;
	}

	/** @return the real width of the source PNG in pixels */
	public int width() {
		return width;
	}

	/** @return the real height of the source PNG in pixels */
	public int height() {
		return height;
	}

	/** Destroys the texture and frees its GPU memory. Safe to call more than once. */
	void close() {
		if (!closed) {
			closed = true;
			MinecraftClient.getInstance().getTextureManager().destroyTexture(id);
		}
	}
}
