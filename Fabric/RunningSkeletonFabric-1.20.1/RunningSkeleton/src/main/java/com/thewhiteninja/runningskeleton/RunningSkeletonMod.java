package com.thewhiteninja.runningskeleton;

import net.fabricmc.api.ModInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Common entrypoint of RunningSkeleton.
 *
 * <p>The mod is client-side only, so this class registers nothing: it only holds the shared
 * constants. All behaviour is initialised from the client entrypoint.</p>
 */
public final class RunningSkeletonMod implements ModInitializer {

	/** Mod identifier, also used as the resource namespace. */
	public static final String MOD_ID = "runningskeleton";

	/** Shared logger. */
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	@Override
	public void onInitialize() {
		LOGGER.info("RunningSkeleton loaded (client-side only mod).");
	}
}
