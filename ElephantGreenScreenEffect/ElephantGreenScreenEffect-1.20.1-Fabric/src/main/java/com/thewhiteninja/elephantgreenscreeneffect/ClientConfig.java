package com.thewhiteninja.elephantgreenscreeneffect;

import com.mojang.logging.LogUtils;
import net.fabricmc.loader.api.FabricLoader;
import org.slf4j.Logger;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Minimal, dependency-free stand-in for the original Forge {@code ForgeConfigSpec}
 * based config. Forge's config API isn't available on Fabric, so this class
 * reads/writes a small TOML-flavoured file at
 * {@code config/elephantgreenscreeneffect-client.toml} by hand, preserving the
 * same file name, the same "general" section, the same "chance" key,
 * the same default (2000) and the same lower bound (1) as the original.
 */
public final class ClientConfig {

    private static final Logger LOGGER = LogUtils.getLogger();

    private static final String FILE_NAME = ElephantGreenScreenEffectClient.MOD_ID + "-client.toml";
    private static final int DEFAULT_CHANCE = 2000;
    private static final int MIN_CHANCE = 1;

    private static final Pattern CHANCE_LINE = Pattern.compile("^\\s*chance\\s*=\\s*(\\d+)\\s*$");

    private static volatile int chance = DEFAULT_CHANCE;

    private ClientConfig() {
    }

    public static int getChance() {
        return chance;
    }

    /**
     * Sets and persists the chance value, matching the original behaviour of
     * {@code ClientConfig.CHANCE.set(value)} followed by {@code ClientConfig.SPEC.save()}.
     */
    public static void setChance(int value) {
        chance = clamp(value);
        save();
    }

    public static void load() {
        Path path = configPath();

        if (!Files.exists(path)) {
            save();
            return;
        }

        try {
            List<String> lines = Files.readAllLines(path, StandardCharsets.UTF_8);
            for (String line : lines) {
                Matcher matcher = CHANCE_LINE.matcher(line);
                if (matcher.matches()) {
                    try {
                        chance = clamp(Integer.parseInt(matcher.group(1)));
                    } catch (NumberFormatException exception) {
                        LOGGER.warn("[ElephantGreenScreenEffect] Invalid chance value in {}, using default {}.",
                                FILE_NAME, DEFAULT_CHANCE);
                        chance = DEFAULT_CHANCE;
                    }
                    return;
                }
            }
            // No "chance" key found (e.g. fresh/blank file) - fall back to default and rewrite it.
            chance = DEFAULT_CHANCE;
            save();
        } catch (IOException exception) {
            LOGGER.warn("[ElephantGreenScreenEffect] Could not read {}, using default chance {}.",
                    FILE_NAME, DEFAULT_CHANCE, exception);
            chance = DEFAULT_CHANCE;
        }
    }

    public static void save() {
        Path path = configPath();

        String content = "#ElephantGreenScreenEffect settings" + System.lineSeparator()
                + System.lineSeparator()
                + "[general]" + System.lineSeparator()
                + "\t#Probability denominator checked once every second." + System.lineSeparator()
                + "\t#A value of 5000 means a 1 in 5000 chance of triggering the animation every second." + System.lineSeparator()
                + "\t#Range: 1 ~ 2147483647" + System.lineSeparator()
                + "\tchance = " + chance + System.lineSeparator();

        try {
            Path parent = path.getParent();
            if (parent != null) {
                Files.createDirectories(parent);
            }
            Files.writeString(path, content, StandardCharsets.UTF_8);
        } catch (IOException exception) {
            LOGGER.warn("[ElephantGreenScreenEffect] Could not save {}.", FILE_NAME, exception);
        }
    }

    private static Path configPath() {
        return FabricLoader.getInstance().getConfigDir().resolve(FILE_NAME);
    }

    private static int clamp(int value) {
        return Math.max(MIN_CHANCE, value);
    }
}
