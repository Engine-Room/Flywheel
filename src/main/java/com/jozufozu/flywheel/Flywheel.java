package com.jozufozu.flywheel;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.jozufozu.flywheel.config.FlwCommands;
import com.jozufozu.flywheel.config.FlwConfig;
import com.jozufozu.flywheel.config.FlwPackets;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

@Mod("flywheel")
public class Flywheel {

	public static final String ID = "flywheel";

	public Flywheel() {
		FMLJavaModLoadingContext.get()
				.getModEventBus()
				.addListener(this::setup);

		MinecraftForge.EVENT_BUS.addListener(FlwCommands::onServerStarting);

		FlwConfig.init();

		DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> FlywheelClient::clientInit);
	}

	private void setup(final FMLCommonSetupEvent event) {
		FlwPackets.registerPackets();
	}
}
