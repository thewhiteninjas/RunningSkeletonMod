package com.thewhiteninja.runningskeleton.animation;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.event.events.player.PlayerDisconnectEvent;
import com.hypixel.hytale.server.core.event.events.player.PlayerReadyEvent;
import com.hypixel.hytale.server.core.plugin.JavaPlugin;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.core.util.Config;
import com.thewhiteninja.runningskeleton.config.RunningSkeletonConfig;
import com.thewhiteninja.runningskeleton.ui.BlankSkeletonHud;
import com.thewhiteninja.runningskeleton.ui.SkeletonRunHud;

import javax.annotation.Nonnull;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;

public class SkeletonAnimationService {

    private static final int ANIMATION_FRAME_COUNT = 14;
    private static final long ANIMATION_TOTAL_DURATION_MILLIS = 2000L;

    private final JavaPlugin plugin;
    private final Config<RunningSkeletonConfig> config;
    private final Map<UUID, Ref<EntityStore>> sessions = new ConcurrentHashMap<>();
    private final Map<UUID, Long> cooldownUntilMillis = new ConcurrentHashMap<>();

    private ScheduledExecutorService scheduler;

    public SkeletonAnimationService(@Nonnull JavaPlugin plugin, @Nonnull Config<RunningSkeletonConfig> config) {
        this.plugin = plugin;
        this.config = config;

        this.plugin.getEventRegistry().registerGlobal(PlayerReadyEvent.class, this::onPlayerReady);
        this.plugin.getEventRegistry().registerGlobal(PlayerDisconnectEvent.class, this::onPlayerDisconnect);
    }

    public void start() {
        this.scheduler = Executors.newSingleThreadScheduledExecutor(runnable -> {
            Thread thread = new Thread(runnable, "RunningSkeleton-Scheduler");
            thread.setDaemon(true);
            return thread;
        });
        this.scheduler.scheduleAtFixedRate(this::tick, 1, 1, TimeUnit.SECONDS);
    }

    public void stop() {
        if (this.scheduler != null) {
            this.scheduler.shutdownNow();
        }
        this.sessions.clear();
        this.cooldownUntilMillis.clear();
    }

    private void onPlayerReady(@Nonnull PlayerReadyEvent event) {
        Ref<EntityStore> ref = event.getPlayerRef();
        if (ref == null || !ref.isValid()) {
            return;
        }

        Store<EntityStore> store = ref.getStore();
        PlayerRef playerRef = store.getComponent(ref, PlayerRef.getComponentType());
        if (playerRef == null) {
            return;
        }

        this.sessions.put(playerRef.getUuid(), ref);
    }

    private void onPlayerDisconnect(@Nonnull PlayerDisconnectEvent event) {
        PlayerRef playerRef = event.getPlayerRef();
        if (playerRef == null) {
            return;
        }

        this.sessions.remove(playerRef.getUuid());
        this.cooldownUntilMillis.remove(playerRef.getUuid());
    }

    private void tick() {
        RunningSkeletonConfig cfg = this.config.get();
        int chance = Math.max(1, cfg.getChance());
        long cooldownMillis = Math.max(0, cfg.getCooldownSeconds()) * 1000L;
        long now = System.currentTimeMillis();

        for (Map.Entry<UUID, Ref<EntityStore>> entry : this.sessions.entrySet()) {
            UUID uuid = entry.getKey();
            Ref<EntityStore> ref = entry.getValue();

            if (!ref.isValid()) {
                this.sessions.remove(uuid);
                continue;
            }

            if (this.isOnCooldown(uuid, now)) {
                continue;
            }

            if (ThreadLocalRandom.current().nextInt(chance) == 0) {
                this.markCooldown(uuid, now, cooldownMillis);
                this.beginAnimation(ref);
            }
        }
    }

    public void forcePlay(@Nonnull PlayerRef playerRef, @Nonnull Store<EntityStore> store,
                           @Nonnull Ref<EntityStore> ref, @Nonnull World world) {
        long cooldownMillis = Math.max(0, this.config.get().getCooldownSeconds()) * 1000L;
        this.markCooldown(playerRef.getUuid(), System.currentTimeMillis(), cooldownMillis);
        this.beginAnimation(ref);
    }

    private void beginAnimation(@Nonnull Ref<EntityStore> ref) {
        if (!ref.isValid()) {
            return;
        }

        Store<EntityStore> store = ref.getStore();
        World world = store.getExternalData().getWorld();
        long startMillis = System.currentTimeMillis();

        world.execute(() -> this.playFrame(ref, store, world, 1, startMillis));
    }

    private void playFrame(@Nonnull Ref<EntityStore> ref, @Nonnull Store<EntityStore> store,
                            @Nonnull World world, int frameNumber, long startMillis) {
        if (!ref.isValid()) {
            return;
        }

        Player player = store.getComponent(ref, Player.getComponentType());
        PlayerRef playerRef = store.getComponent(ref, PlayerRef.getComponentType());
        if (player == null || playerRef == null) {
            return;
        }

        if (frameNumber > ANIMATION_FRAME_COUNT) {
            player.getHudManager().addCustomHud(playerRef, new BlankSkeletonHud(playerRef));
            return;
        }

        player.getHudManager().addCustomHud(playerRef, new SkeletonRunHud(playerRef, frameNumber));

        long targetMillis = startMillis + Math.round(frameNumber * (ANIMATION_TOTAL_DURATION_MILLIS / (double) ANIMATION_FRAME_COUNT));
        long delayMillis = Math.max(0, targetMillis - System.currentTimeMillis());

        this.scheduler.schedule(
                () -> world.execute(() -> this.playFrame(ref, store, world, frameNumber + 1, startMillis)),
                delayMillis,
                TimeUnit.MILLISECONDS
        );
    }

    private void markCooldown(@Nonnull UUID uuid, long now, long cooldownMillis) {
        this.cooldownUntilMillis.put(uuid, now + cooldownMillis);
    }

    public boolean isOnCooldown(@Nonnull UUID uuid) {
        return this.isOnCooldown(uuid, System.currentTimeMillis());
    }

    private boolean isOnCooldown(@Nonnull UUID uuid, long now) {
        Long until = this.cooldownUntilMillis.get(uuid);
        return until != null && until > now;
    }

    public int getActiveCooldownCount() {
        long now = System.currentTimeMillis();
        int count = 0;
        for (long until : this.cooldownUntilMillis.values()) {
            if (until > now) {
                count++;
            }
        }
        return count;
    }
}
