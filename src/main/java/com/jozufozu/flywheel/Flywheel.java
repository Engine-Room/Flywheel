package com.jozufozu.flywheel;

import org.apache.maven.artifact.versioning.ArtifactVersion;
import org.slf4j.Logger;

import com.jozufozu.flywheel.backend.Backend;
import com.jozufozu.flywheel.backend.engine.batching.DrawBuffer;
import com.jozufozu.flywheel.backend.instancing.InstancedRenderDispatcher;
import com.jozufozu.flywheel.config.BackendTypeArgument;
import com.jozufozu.flywheel.config.FlwCommands;
import com.jozufozu.flywheel.config.FlwConfig;
import com.jozufozu.flywheel.handler.EntityWorldHandler;
import com.jozufozu.flywheel.handler.ForgeEvents;
import com.jozufozu.flywheel.lib.backend.BackendTypes;
import com.jozufozu.flywheel.lib.context.Contexts;
import com.jozufozu.flywheel.lib.format.Formats;
import com.jozufozu.flywheel.lib.material.Materials;
import com.jozufozu.flywheel.lib.model.Models;
import com.jozufozu.flywheel.lib.model.PartialModel;
import com.jozufozu.flywheel.lib.pipeline.Pipelines;
import com.jozufozu.flywheel.lib.struct.StructTypes;
import com.jozufozu.flywheel.lib.util.QuadConverter;
import com.jozufozu.flywheel.lib.util.RenderWork;
import com.jozufozu.flywheel.lib.util.ShadersModHandler;
import com.jozufozu.flywheel.mixin.PausedPartialTickAccessor;
import com.jozufozu.flywheel.vanilla.VanillaInstances;
import com.mojang.logging.LogUtils;

import net.minecraft.commands.synchronization.ArgumentTypes;
import net.minecraft.commands.synchronization.EmptyArgumentSerializer;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.CrashReportCallables;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.IExtensionPoint;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.network.NetworkConstants;

@Mod(Flywheel.ID)
public class Flywheel {

	public static final String ID = "flywheel";
	public static final Logger LOGGER = LogUtils.getLogger();
	private static ArtifactVersion version;

	public Flywheel() {
		ModLoadingContext modLoadingContext = ModLoadingContext.get();

		version = modLoadingContext
				.getActiveContainer()
				.getModInfo()
				.getVersion();

		IEventBus forgeEventBus = MinecraftForge.EVENT_BUS;
		IEventBus modEventBus = FMLJavaModLoadingContext.get()
				.getModEventBus();
		modEventBus.addListener(Flywheel::setup);

		FlwConfig.init();

		modLoadingContext.registerExtensionPoint(IExtensionPoint.DisplayTest.class, () -> new IExtensionPoint.DisplayTest(
				() -> NetworkConstants.IGNORESERVERONLY,
				(serverVersion, isNetwork) -> isNetwork
		));

		DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> Flywheel.clientInit(forgeEventBus, modEventBus));
	}

	private static void clientInit(IEventBus forgeEventBus, IEventBus modEventBus) {
		CrashReportCallables.registerCrashCallable("Flywheel Backend", Backend::getBackendDescriptor);

		ShadersModHandler.init();
		BackendTypes.init();
		Backend.init();

		forgeEventBus.addListener(FlwCommands::registerClientCommands);

		forgeEventBus.addListener(EventPriority.HIGHEST, QuadConverter::onReloadRenderers);
		forgeEventBus.addListener(Models::onReloadRenderers);
		forgeEventBus.addListener(DrawBuffer::onReloadRenderers);

		forgeEventBus.addListener(InstancedRenderDispatcher::onReloadRenderers);
		forgeEventBus.addListener(InstancedRenderDispatcher::onRenderStage);
		forgeEventBus.addListener(InstancedRenderDispatcher::onBeginFrame);
		forgeEventBus.addListener(InstancedRenderDispatcher::tick);

		forgeEventBus.addListener(EntityWorldHandler::onEntityJoinWorld);
		forgeEventBus.addListener(EntityWorldHandler::onEntityLeaveWorld);

		forgeEventBus.addListener(ForgeEvents::addToDebugScreen);
		forgeEventBus.addListener(ForgeEvents::unloadWorld);
		forgeEventBus.addListener(ForgeEvents::tickLight);

		forgeEventBus.addListener(EventPriority.LOWEST, RenderWork::onRenderLevelLast);

		modEventBus.addListener(PartialModel::onModelRegistry);
		modEventBus.addListener(PartialModel::onModelBake);

//		forgeEventBus.addListener(ExampleEffect::tick);
//		forgeEventBus.addListener(ExampleEffect::onReload);

		Formats.init();
		StructTypes.init();
		Materials.init();
		Contexts.init();
		Pipelines.init();

		VanillaInstances.init();

		// https://github.com/Jozufozu/Flywheel/issues/69
		// Weird issue with accessor loading.
		// Only thing I've seen that's close to a fix is to force the class to load before trying to use it.
		// From the SpongePowered discord:
		// https://discord.com/channels/142425412096491520/626802111455297538/675007581168599041
		LOGGER.debug("Successfully loaded {}", PausedPartialTickAccessor.class.getName());
	}

	private static void setup(final FMLCommonSetupEvent event) {
		ArgumentTypes.register(rl("engine").toString(), BackendTypeArgument.class, new EmptyArgumentSerializer<>(BackendTypeArgument::getInstance));
	}

	public static ArtifactVersion getVersion() {
		return version;
	}

	public static ResourceLocation rl(String path) {
		return new ResourceLocation(ID, path);
	}
}
