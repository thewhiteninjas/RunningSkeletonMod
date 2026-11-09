package com.thewhiteninja.runningskeleton;

import com.mojang.logging.LogUtils;
import com.thewhiteninja.runningskeleton.config.SkeletonConfig;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import org.slf4j.Logger;

/**
 * Main class of the RunningSkeleton mod.
 *
 * <p>This class is intentionally free of any client-only references so that it can be loaded safely
 * on any physical side. All client logic lives in the {@code client} and {@code command} packages
 * and is only wired up through event subscribers restricted to {@code Dist.CLIENT}.</p>
 */
@Mod(RunningSkeletonMod.MOD_ID)
public final class RunningSkeletonMod {

    /** The mod id. Must match mods.toml and the assets folder name. */
    public static final String MOD_ID = "runningskeleton";

    /** Shared logger for the whole mod. */
    public static final Logger LOGGER = LogUtils.getLogger();

    public RunningSkeletonMod() {
        // CLIENT config: stored in the local config folder and never synced with a server.
        ModLoadingContext.get().registerConfig(
                ModConfig.Type.CLIENT,
                SkeletonConfig.SPEC,
                MOD_ID + "-client.toml");
    }
}
