package com.thewhiteninja.runningskeleton;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;

/**
 * Entry point of the RunningSkeleton mod.
 * This class is only loaded on the physical client: the mod has no server-side behavior.
 */
@Mod(value = RunningSkeletonMod.MOD_ID, dist = Dist.CLIENT)
public final class RunningSkeletonMod {

    public static final String MOD_ID = "runningskeleton";

    public RunningSkeletonMod(ModContainer modContainer) {
        modContainer.registerConfig(ModConfig.Type.CLIENT, RunningSkeletonConfig.SPEC);
    }
}
