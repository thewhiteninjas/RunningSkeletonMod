package com.runningskeleton.mod;

import com.runningskeleton.mod.client.overlay.SkeletonOverlay;
import com.runningskeleton.mod.command.SkeletonCommand;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.RegisterGuiLayersEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.event.server.ServerStartingEvent;

@Mod(RunningSkeleton.MOD_ID)
public class RunningSkeleton {

    public static final String MOD_ID = "runningskeleton";

    public RunningSkeleton(IEventBus modEventBus) {
        // Register client setup
        modEventBus.addListener(this::clientSetup);
        // Register GUI layers (overlay) on mod bus
        modEventBus.addListener(this::registerGuiLayers);

        // Register game events
        NeoForge.EVENT_BUS.addListener(this::onServerStarting);
        NeoForge.EVENT_BUS.addListener(this::onRegisterCommands);
    }

    private void clientSetup(final FMLClientSetupEvent event) {
        // Client-side initialization
    }

    private void registerGuiLayers(final RegisterGuiLayersEvent event) {
        // Register our fullscreen overlay above everything
        event.registerAboveAll(
            net.minecraft.resources.ResourceLocation.fromNamespaceAndPath(MOD_ID, "skeleton_overlay"),
            SkeletonOverlay.INSTANCE
        );
    }

    private void onServerStarting(final ServerStartingEvent event) {
        // Server starting
    }

    private void onRegisterCommands(final RegisterCommandsEvent event) {
        SkeletonCommand.register(event.getDispatcher());
    }
}
