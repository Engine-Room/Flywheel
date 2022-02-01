package com.jozufozu.flywheel;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.maven.artifact.versioning.ArtifactVersion;

import com.jozufozu.flywheel.backend.Backend;
import com.jozufozu.flywheel.backend.OptifineHandler;
import com.jozufozu.flywheel.config.EngineArgument;
import com.jozufozu.flywheel.config.FlwCommands;
import com.jozufozu.flywheel.config.FlwConfig;
import com.jozufozu.flywheel.core.Contexts;
import com.jozufozu.flywheel.core.PartialModel;
import com.jozufozu.flywheel.core.StitchedSprite;
import com.jozufozu.flywheel.core.compile.ProgramCompiler;
import com.jozufozu.flywheel.event.ReloadRenderersEvent;
import com.jozufozu.flywheel.mixin.PausedPartialTickAccessor;
import com.jozufozu.flywheel.vanilla.VanillaInstances;

import net.minecraft.commands.synchronization.ArgumentTypes;
import net.minecraft.commands.synchronization.EmptyArgumentSerializer;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.CrashReportCallables;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.forgespi.language.IModFileInfo;

@Mod(Flywheel.ID)
public class Flywheel {

	public static final String ID = "flywheel";
	public static final Logger LOGGER = LogManager.getLogger(Flywheel.class);
	public static ArtifactVersion VERSION;

	public Flywheel() {
		IModFileInfo modFileById = ModList.get()
				.getModFileById(ID);

		VERSION = modFileById.getMods()
				.get(0)
				.getVersion();

		FMLJavaModLoadingContext.get()
				.getModEventBus()
				.addListener(this::setup);

		FlwConfig.init();

		DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> Flywheel::clientInit);
	}

	public static ResourceLocation rl(String path) {
		return new ResourceLocation(ID, path);
	}

	public static void clientInit() {
		CrashReportCallables.registerCrashCallable("Flywheel Backend", Backend::getBackendDescriptor);

		OptifineHandler.init();
		Backend.init();
		IEventBus modEventBus = FMLJavaModLoadingContext.get()
				.getModEventBus();

		modEventBus.addListener(Contexts::flwInit);
		modEventBus.addListener(PartialModel::onModelRegistry);
		modEventBus.addListener(PartialModel::onModelBake);
		modEventBus.addListener(StitchedSprite::onTextureStitchPre);
		modEventBus.addListener(StitchedSprite::onTextureStitchPost);

		MinecraftForge.EVENT_BUS.addListener(FlwCommands::registerClientCommands);

		MinecraftForge.EVENT_BUS.<ReloadRenderersEvent>addListener(ProgramCompiler::invalidateAll);

		VanillaInstances.init();

		// https://github.com/Jozufozu/Flywheel/issues/69
		// Weird issue with accessor loading.
		// Only thing I've seen that's close to a fix is to force the class to load before trying to use it.
		// From the SpongePowered discord:
		// https://discord.com/channels/142425412096491520/626802111455297538/675007581168599041
		LOGGER.info("Successfully loaded {}", PausedPartialTickAccessor.class.getName());
	}

	private void setup(final FMLCommonSetupEvent event) {
		ArgumentTypes.register(rl("engine").toString(), EngineArgument.class, new EmptyArgumentSerializer<>(EngineArgument::getInstance));
	}
}
