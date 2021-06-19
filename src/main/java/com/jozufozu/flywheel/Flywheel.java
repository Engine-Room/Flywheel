package com.jozufozu.flywheel;

import com.jozufozu.flywheel.config.FlwConfig;
import com.jozufozu.flywheel.config.FlwCommands;

import com.jozufozu.flywheel.config.FlwPackets;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Mod("flywheel")
public class Flywheel {

    public static final String ID = "flywheel";
    private static final Logger LOGGER = LogManager.getLogger();

    public Flywheel() {
		FMLJavaModLoadingContext.get().getModEventBus().addListener(this::setup);

		MinecraftForge.EVENT_BUS.addListener(FlwCommands::onServerStarting);

		FlwConfig.init();

		DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> Client::clientInit);
	}

	private void setup(final FMLCommonSetupEvent event) {
		FlwPackets.registerPackets();
	}
}
