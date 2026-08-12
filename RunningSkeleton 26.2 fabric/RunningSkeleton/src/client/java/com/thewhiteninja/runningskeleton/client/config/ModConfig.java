package com.thewhiteninja.runningskeleton.client.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.thewhiteninja.runningskeleton.client.RunningSkeletonClient;
import net.fabricmc.loader.api.FabricLoader;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Persistent client-side configuration for RunningSkeleton.
 * Stored as JSON in the standard Fabric config directory.
 */
public final class ModConfig {

	public static final int DEFAULT_CHANCE_DENOMINATOR = 2000;

	private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
	private static final Path CONFIG_PATH = FabricLoader.getInstance()
			.getConfigDir()
			.resolve("runningskeleton.json");

	private int chanceDenominator = DEFAULT_CHANCE_DENOMINATOR;

	public int chanceDenominator() {
		return chanceDenominator;
	}

	public void setChanceDenominator(int chanceDenominator) {
		this.chanceDenominator = chanceDenominator;
	}

	public static ModConfig load() {
		if (!Files.exists(CONFIG_PATH)) {
			ModConfig defaultConfig = new ModConfig();
			defaultConfig.save();
			return defaultConfig;
		}

		try {
			String json = Files.readString(CONFIG_PATH, StandardCharsets.UTF_8);
			ModConfig loaded = GSON.fromJson(json, ModConfig.class);

			if (loaded == null || loaded.chanceDenominator <= 0) {
				RunningSkeletonClient.LOGGER.warn("Invalid RunningSkeleton config, restoring defaults.");
				ModConfig defaultConfig = new ModConfig();
				defaultConfig.save();
				return defaultConfig;
			}

			return loaded;
		} catch (IOException exception) {
			RunningSkeletonClient.LOGGER.error("Failed to read RunningSkeleton config, using defaults.", exception);
			return new ModConfig();
		}
	}

	public void save() {
		try {
			Files.createDirectories(CONFIG_PATH.getParent());
			Files.writeString(CONFIG_PATH, GSON.toJson(this), StandardCharsets.UTF_8);
		} catch (IOException exception) {
			RunningSkeletonClient.LOGGER.error("Failed to save RunningSkeleton config.", exception);
		}
	}
}
