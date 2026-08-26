package com.runningskeleton.mod;

import com.mojang.logging.LogUtils;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.slf4j.Logger;

@Mod(RunningSkeletonMod.MOD_ID)
public class RunningSkeletonMod {

    public static final String MOD_ID = "runningskeleton";
    private static final Logger LOGGER = LogUtils.getLogger();

    public RunningSkeletonMod() {
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();
        modEventBus.addListener(this::onClientSetup);
        MinecraftForge.EVENT_BUS.register(this);
    }

    private void onClientSetup(final FMLClientSetupEvent event) {
        LOGGER.info("RunningSkeleton mod initialized!");
        MinecraftForge.EVENT_BUS.register(SkeletonEventHandler.class);
        MinecraftForge.EVENT_BUS.register(SkeletonCommandHandler.class);
    }
}
