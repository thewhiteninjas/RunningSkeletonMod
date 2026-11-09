package com.thewhiteninja.runningskeleton.client.frames;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.platform.TextureUtil;
import com.mojang.blaze3d.systems.RenderSystem;
import com.thewhiteninja.runningskeleton.RunningSkeletonMod;
import net.minecraft.client.texture.NativeImage;
import net.minecraft.resource.Resource;
import net.minecraft.resource.ResourceManager;
import net.minecraft.util.Identifier;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;

import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;
import java.util.Map;
import java.util.Optional;
import java.util.TreeMap;
import java.util.concurrent.CancellationException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Decodes PNG frames on background threads and owns the GPU textures created from them.
 *
 * <p>All public methods must be called from the render (client) thread. Decoding happens on a
 * small daemon thread pool; decoded images are uploaded here, at most
 * {@link #MAX_UPLOADS_PER_PUMP} per call, and closed right after the upload. Each frame keeps
 * its own real size, so frames of different dimensions are supported.</p>
 */
public final class FrameTextureManager {

	/** Total number of frames in the animation. */
	public static final int TOTAL_FRAMES = 79;
	/** Maximum number of frames decoded/uploaded ahead of the playback position. */
	public static final int MAX_AHEAD = 12;
	/** Maximum number of GPU uploads per {@link #pump(int)} call. */
	public static final int MAX_UPLOADS_PER_PUMP = 3;

	private final ExecutorService executor = Executors.newFixedThreadPool(2, runnable -> {
		Thread thread = new Thread(runnable, "RunningSkeleton-Decoder");
		thread.setDaemon(true);
		return thread;
	});

	private final TreeMap<Integer, CompletableFuture<DecodedFrame>> pending = new TreeMap<>();
	private final TreeMap<Integer, GpuFrame> textures = new TreeMap<>();

	private AtomicBoolean cancelToken = new AtomicBoolean(true);
	private ResourceManager resourceManager;
	private int nextToSchedule;
	private boolean active;
	private boolean shutDown;

	/**
	 * Returns the file name of a frame.
	 *
	 * @param index zero-based frame index
	 * @return for example {@code frame_0001.png} for index 0
	 */
	public static String fileName(int index) {
		return String.format("frame_%04d.png", index + 1);
	}

	/**
	 * Starts a new loading session, releasing anything left from a previous one.
	 *
	 * @param manager the client resource manager to read the PNG files from
	 */
	public void begin(ResourceManager manager) {
		release();
		if (shutDown) {
			return;
		}
		cancelToken = new AtomicBoolean(false);
		resourceManager = manager;
		nextToSchedule = 0;
		active = true;
	}

	/**
	 * Schedules decoding, uploads finished frames and frees frames that are no longer needed.
	 *
	 * @param head the frame index currently being displayed (0 while loading)
	 * @return the outcome; on {@code MISSING} or {@code FAILED} the caller must stop the animation
	 */
	public PumpResult pump(int head) {
		if (!active) {
			return PumpResult.SUCCESS;
		}
		if (nextToSchedule < head) {
			nextToSchedule = head;
		}
		while (nextToSchedule < TOTAL_FRAMES && nextToSchedule < head + MAX_AHEAD) {
			schedule(nextToSchedule++);
		}

		int uploads = 0;
		Iterator<Map.Entry<Integer, CompletableFuture<DecodedFrame>>> iterator = pending.entrySet().iterator();
		while (iterator.hasNext() && uploads < MAX_UPLOADS_PER_PUMP) {
			Map.Entry<Integer, CompletableFuture<DecodedFrame>> entry = iterator.next();
			if (!entry.getValue().isDone()) {
				continue;
			}
			iterator.remove();
			DecodedFrame decoded = takeResult(entry.getValue(), entry.getKey());
			switch (decoded.kind()) {
				case MISSING:
					return PumpResult.missing(decoded.fileName());
				case FAILED:
					return PumpResult.failed(decoded.fileName(), decoded.error());
				case SKIPPED:
					break;
				case OK:
					if (decoded.index() < head) {
						decoded.close();
						break;
					}
					Throwable uploadError = upload(decoded);
					if (uploadError != null) {
						return PumpResult.failed(decoded.fileName(), uploadError);
					}
					uploads++;
					break;
				default:
					decoded.close();
					break;
			}
		}

		trim(head);
		return PumpResult.SUCCESS;
	}

	/**
	 * Counts the uploaded frames that are contiguous from frame 0.
	 *
	 * @return the number of leading frames ready to display
	 */
	public int contiguousReady() {
		int count = 0;
		while (count < TOTAL_FRAMES && textures.containsKey(count)) {
			count++;
		}
		return count;
	}

	/**
	 * Returns the most recent uploaded frame at or before the given index.
	 *
	 * @param index the wanted frame index
	 * @return the frame, or {@code null} if none is available yet
	 */
	public GpuFrame frameFor(int index) {
		Map.Entry<Integer, GpuFrame> entry = textures.floorEntry(index);
		return entry == null ? null : entry.getValue();
	}

	/**
	 * Cancels the session and frees every GPU texture and every decoded image.
	 *
	 * <p>Futures are never cancelled: each unconsumed future gets a callback that closes its
	 * image as soon as it completes, so no {@link NativeImage} can leak.</p>
	 */
	public void release() {
		active = false;
		cancelToken.set(true);
		for (CompletableFuture<DecodedFrame> future : pending.values()) {
			future.whenComplete((decoded, error) -> {
				if (decoded != null) {
					decoded.close();
				}
			});
		}
		pending.clear();
		for (GpuFrame frame : textures.values()) {
			frame.close();
		}
		textures.clear();
		resourceManager = null;
	}

	/** Releases everything and stops the decoder threads. The manager cannot be reused afterwards. */
	public void shutdown() {
		release();
		shutDown = true;
		executor.shutdown();
	}

	private void schedule(int index) {
		String name = fileName(index);
		Identifier id = Identifier.of(RunningSkeletonMod.MOD_ID, "textures/animation/" + name);
		AtomicBoolean token = cancelToken;
		ResourceManager manager = resourceManager;
		CompletableFuture<DecodedFrame> future =
				CompletableFuture.supplyAsync(() -> decode(manager, id, index, name, token), executor);
		pending.put(index, future);
	}

	/** Runs on a background thread. Never touches OpenGL. */
	private static DecodedFrame decode(ResourceManager manager, Identifier id, int index, String name,
									   AtomicBoolean token) {
		if (token.get()) {
			return DecodedFrame.skipped(index, name);
		}
		try {
			Optional<Resource> resource = manager.getResource(id);
			if (resource.isEmpty()) {
				return DecodedFrame.missing(index, name);
			}
			try (InputStream stream = resource.get().getInputStream()) {
				return DecodedFrame.ok(index, name, NativeImage.read(stream));
			}
		} catch (IOException | RuntimeException e) {
			return DecodedFrame.failed(index, name, e);
		}
	}

	private static DecodedFrame takeResult(CompletableFuture<DecodedFrame> future, int index) {
		try {
			return future.join();
		} catch (CompletionException | CancellationException e) {
			return DecodedFrame.failed(index, fileName(index), e.getCause() != null ? e.getCause() : e);
		}
	}

	/**
	 * Uploads a decoded image to a new GPU texture and closes the image.
	 *
	 * @return {@code null} on success, otherwise the error
	 */
	private Throwable upload(DecodedFrame decoded) {
		NativeImage image = decoded.takeImage();
		if (image == null) {
			return new IllegalStateException("Decoded image missing for " + decoded.fileName());
		}
		int id = -1;
		try {
			int width = image.getWidth();
			int height = image.getHeight();
			id = GlStateManager._genTexture();
			TextureUtil.prepareImage(id, width, height);
			RenderSystem.bindTexture(id);
			GlStateManager._texParameter(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_LINEAR);
			GlStateManager._texParameter(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_LINEAR);
			GlStateManager._texParameter(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_S, GL12.GL_CLAMP_TO_EDGE);
			GlStateManager._texParameter(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_T, GL12.GL_CLAMP_TO_EDGE);
			image.upload(0, 0, 0, true);
			textures.put(decoded.index(), new GpuFrame(id, width, height));
			return null;
		} catch (RuntimeException e) {
			if (id != -1) {
				TextureUtil.releaseTextureId(id);
			}
			return e;
		} finally {
			image.close();
		}
	}

	/** Frees textures older than the frame currently displayed (that one is kept as fallback). */
	private void trim(int head) {
		Integer keep = textures.floorKey(head);
		if (keep == null) {
			return;
		}
		Iterator<Map.Entry<Integer, GpuFrame>> iterator = textures.headMap(keep, false).entrySet().iterator();
		while (iterator.hasNext()) {
			iterator.next().getValue().close();
			iterator.remove();
		}
	}
}
