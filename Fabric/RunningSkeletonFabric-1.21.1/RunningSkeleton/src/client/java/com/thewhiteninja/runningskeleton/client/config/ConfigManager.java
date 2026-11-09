package com.thewhiteninja.runningskeleton.client.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonParseException;
import com.thewhiteninja.runningskeleton.RunningSkeletonMod;
import net.fabricmc.loader.api.FabricLoader;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

/**
 * Loads and saves the client configuration ({@code runningskeleton-client.json}).
 *
 * <p>Missing, unreadable or corrupted files are replaced with the default values.</p>
 */
public final class ConfigManager {

	/** Default animation probability denominator (1 in N per second). */
	public static final int DEFAULT_CHANCE = 2000;

	private static final String FILE_NAME = "runningskeleton-client.json";
	private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

	private final Path file;
	private volatile int chance = DEFAULT_CHANCE;

	/** Creates a manager pointing at the Fabric config directory. */
	public ConfigManager() {
		this.file = FabricLoader.getInstance().getConfigDir().resolve(FILE_NAME);
	}

	/** On-disk representation of the configuration. */
	private static final class Data {
		private int chance;

		private Data(int chance) {
			this.chance = chance;
		}
	}

	/** Loads the configuration from disk, restoring defaults if the file is missing or invalid. */
	public synchronized void load() {
		if (!Files.exists(file)) {
			chance = DEFAULT_CHANCE;
			save();
			return;
		}
		try (Reader reader = Files.newBufferedReader(file, StandardCharsets.UTF_8)) {
			Data data = GSON.fromJson(reader, Data.class);
			if (data == null || data.chance < 1) {
				throw new JsonParseException("Missing or invalid 'chance' value");
			}
			chance = data.chance;
		} catch (IOException | RuntimeException e) {
			RunningSkeletonMod.LOGGER.warn("Invalid {}; restoring defaults", FILE_NAME, e);
			chance = DEFAULT_CHANCE;
			save();
		}
	}

	/**
	 * Returns the current probability denominator.
	 *
	 * @return N in "1 in N per second"
	 */
	public int getChance() {
		return chance;
	}

	/**
	 * Updates the probability denominator and saves it to disk immediately.
	 *
	 * @param newChance value greater than or equal to 1
	 * @return {@code true} if the value was written to disk
	 * @throws IllegalArgumentException if the value is lower than 1
	 */
	public synchronized boolean setChance(int newChance) {
		if (newChance < 1) {
			throw new IllegalArgumentException("chance must be >= 1");
		}
		chance = newChance;
		return save();
	}

	/**
	 * Writes the configuration atomically (temp file + move).
	 *
	 * @return {@code true} on success
	 */
	public synchronized boolean save() {
		Path tmp = file.resolveSibling(FILE_NAME + ".tmp");
		try {
			Files.createDirectories(file.getParent());
			try (Writer writer = Files.newBufferedWriter(tmp, StandardCharsets.UTF_8)) {
				GSON.toJson(new Data(chance), writer);
			}
			try {
				Files.move(tmp, file, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
			} catch (AtomicMoveNotSupportedException e) {
				Files.move(tmp, file, StandardCopyOption.REPLACE_EXISTING);
			}
			return true;
		} catch (IOException e) {
			RunningSkeletonMod.LOGGER.error("Could not save {}", FILE_NAME, e);
			return false;
		}
	}
}
