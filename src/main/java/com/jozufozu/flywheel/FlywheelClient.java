package com.jozufozu.flywheel;

import com.jozufozu.flywheel.backend.Backend;
import com.jozufozu.flywheel.core.AtlasStitcher;
import com.jozufozu.flywheel.core.Contexts;
import com.jozufozu.flywheel.core.Materials;
import com.jozufozu.flywheel.core.PartialModel;
import com.jozufozu.flywheel.vanilla.VanillaInstances;

import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

public class FlywheelClient {

	public static void clientInit() {

		Backend.init();
		IEventBus modEventBus = FMLJavaModLoadingContext.get()
				.getModEventBus();

		modEventBus.addListener(AtlasStitcher.getInstance()::onTextureStitch);

		modEventBus.addListener(Contexts::flwInit);
		modEventBus.addListener(Materials::flwInit);
		modEventBus.addListener(PartialModel::onModelRegistry);
		modEventBus.addListener(PartialModel::onModelBake);

		VanillaInstances.init();
	}
}
