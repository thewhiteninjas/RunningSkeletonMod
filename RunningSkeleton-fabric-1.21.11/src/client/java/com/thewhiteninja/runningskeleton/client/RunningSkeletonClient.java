package com.thewhiteninja.runningskeleton.client;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;

import java.util.Random;

/**
 * Client entrypoint for the RunningSkeleton mod.
 * <p>
 * Every second (20 client ticks), if the animation is not currently playing
 * and not on cooldown, performs a random chance check. On success, the
 * fullscreen animation is played; once it finishes, a 5 second cooldown
 * starts during which no new animation can trigger.
 */
public final class RunningSkeletonClient implements ClientModInitializer {

    private static final Random RANDOM = new Random();

    @Override
    public void onInitializeClient() {
        SkeletonCommands.register();

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            // Always advance the animation playback and cooldown timer first.
            AnimationHandler.tick();
            SkeletonState.tickCooldown();

            // Only perform the random check once per second, and only when
            // the animation isn't already playing or on cooldown.
            if (SkeletonState.shouldPerformCheck()) {
                if (!AnimationHandler.isPlaying() && !SkeletonState.isOnCooldown()) {
                    int chance = SkeletonState.getChance();
                    if (chance > 0 && RANDOM.nextInt(chance) == 0) {
                        AnimationHandler.start();
                    }
                }
            }
        });

        HudRenderCallback.EVENT.register((drawContext, tickCounter) -> AnimationHandler.render(drawContext));
    }
}
