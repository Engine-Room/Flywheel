package com.jozufozu.flywheel;

import com.jozufozu.flywheel.backend.Backend;
import com.jozufozu.flywheel.core.Contexts;
import com.jozufozu.flywheel.core.Materials;
import com.jozufozu.flywheel.core.PartialModel;
import com.jozufozu.flywheel.core.StitchedSprite;
import com.jozufozu.flywheel.mixin.PausedPartialTickAccessor;
import com.jozufozu.flywheel.vanilla.VanillaInstances;

import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.CrashReportCallables;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

public class FlywheelClient {

	public static void clientInit() {

		CrashReportCallables.registerCrashCallable("Flywheel Backend", () ->
				Backend.getInstance().getBackendDescriptor());

		Backend.init();
		IEventBus modEventBus = FMLJavaModLoadingContext.get()
				.getModEventBus();

		modEventBus.addListener(Contexts::flwInit);
		modEventBus.addListener(Materials::flwInit);
		modEventBus.addListener(PartialModel::onModelRegistry);
		modEventBus.addListener(PartialModel::onModelBake);
		modEventBus.addListener(StitchedSprite::onTextureStitchPre);
		modEventBus.addListener(StitchedSprite::onTextureStitchPost);

		VanillaInstances.init();

		// https://github.com/Jozufozu/Flywheel/issues/69
		// Weird issue with accessor loading.
		// Only thing I've seen that's close to a fix is to force the class to load before trying to use it.
		// From the SpongePowered discord:
		// https://discord.com/channels/142425412096491520/626802111455297538/675007581168599041
		Flywheel.LOGGER.info("Successfully loaded {}", PausedPartialTickAccessor.class.getName());
	}
}
