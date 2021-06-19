package com.jozufozu.flywheel;

import com.jozufozu.flywheel.backend.Backend;
import com.jozufozu.flywheel.core.AtlasStitcher;

import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

public class Client {

	public static void clientInit() {

		Backend.init();
		FMLJavaModLoadingContext.get().getModEventBus().addListener(AtlasStitcher.getInstance()::onTextureStitch);
	}
}
