package com.thewhiteninja.runningskeleton;

import com.mojang.logging.LogUtils;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import org.slf4j.Logger;

/**
 * Entry point of the RunningSkeleton mod.
 *
 * <p>The mod is client-only: the {@code dist} parameter prevents this class from being loaded on a
 * dedicated server. This class deliberately does not reference any Minecraft client class; all
 * client behaviour is attached through {@code @EventBusSubscriber} classes in the {@code client}
 * package, which NeoForge discovers automatically.</p>
 */
@Mod(value = RunningSkeletonMod.MOD_ID, dist = Dist.CLIENT)
public final class RunningSkeletonMod {

    /** The mod id. Must match neoforge.mods.toml and the assets folder name. */
    public static final String MOD_ID = "runningskeleton";

    /** Shared logger for the whole mod. */
    public static final Logger LOGGER = LogUtils.getLogger();

    /**
     * Creates the mod instance and registers the client configuration.
     *
     * @param container the container of this mod, injected by NeoForge
     */
    public RunningSkeletonMod(ModContainer container) {
        container.registerConfig(ModConfig.Type.CLIENT, SkeletonConfig.SPEC);
    }
}
