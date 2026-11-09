package com.thewhiteninja.runningskeleton.client;

import com.thewhiteninja.runningskeleton.RunningSkeletonMod;
import com.thewhiteninja.runningskeleton.command.SkeletonCommands;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.RegisterClientCommandsEvent;

/**
 * Client events delivered on the game event bus.
 */
@EventBusSubscriber(modid = RunningSkeletonMod.MOD_ID, value = Dist.CLIENT)
public final class ClientGameEvents {

    private ClientGameEvents() {
    }

    /**
     * Advances the animation state machine once per client tick.
     *
     * @param event the client tick event
     */
    @SubscribeEvent
    public static void onClientTick(ClientTickEvent.Post event) {
        AnimationController.get().tick();
    }

    /**
     * Stops the animation and frees every texture when the player leaves the world.
     *
     * @param event the logout event
     */
    @SubscribeEvent
    public static void onLoggingOut(ClientPlayerNetworkEvent.LoggingOut event) {
        AnimationController.get().reset();
    }

    /**
     * Registers the client-side {@code /skeleton} command, which is never sent to the server.
     *
     * @param event the client command registration event
     */
    @SubscribeEvent
    public static void onRegisterClientCommands(RegisterClientCommandsEvent event) {
        SkeletonCommands.register(event.getDispatcher());
    }
}
