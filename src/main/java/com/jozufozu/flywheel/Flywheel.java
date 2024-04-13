package com.jozufozu.flywheel;

import java.util.ArrayList;

import org.apache.maven.artifact.versioning.ArtifactVersion;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jozufozu.flywheel.api.event.EndClientResourceReloadEvent;
import com.jozufozu.flywheel.api.visualization.VisualizationManager;
import com.jozufozu.flywheel.backend.Backends;
import com.jozufozu.flywheel.backend.ShaderIndices;
import com.jozufozu.flywheel.backend.compile.FlwPrograms;
import com.jozufozu.flywheel.backend.engine.uniform.Uniforms;
import com.jozufozu.flywheel.config.BackendArgument;
import com.jozufozu.flywheel.config.FlwCommands;
import com.jozufozu.flywheel.config.FlwConfig;
import com.jozufozu.flywheel.impl.BackendManagerImpl;
import com.jozufozu.flywheel.impl.registry.IdRegistryImpl;
import com.jozufozu.flywheel.impl.registry.RegistryImpl;
import com.jozufozu.flywheel.impl.visualization.VisualizationEventHandler;
import com.jozufozu.flywheel.lib.instance.InstanceTypes;
import com.jozufozu.flywheel.lib.material.CutoutShaders;
import com.jozufozu.flywheel.lib.material.FogShaders;
import com.jozufozu.flywheel.lib.material.StandardMaterialShaders;
import com.jozufozu.flywheel.lib.memory.FlwMemoryTracker;
import com.jozufozu.flywheel.lib.model.ModelCache;
import com.jozufozu.flywheel.lib.model.ModelHolder;
import com.jozufozu.flywheel.lib.model.baked.PartialModel;
import com.jozufozu.flywheel.lib.model.part.MeshTree;
import com.jozufozu.flywheel.lib.util.LevelAttached;
import com.jozufozu.flywheel.lib.util.ShadersModHandler;
import com.jozufozu.flywheel.lib.util.StringUtil;
import com.jozufozu.flywheel.vanilla.VanillaVisuals;

import net.minecraft.client.Minecraft;
import net.minecraft.commands.synchronization.ArgumentTypeInfos;
import net.minecraft.commands.synchronization.SingletonArgumentInfo;
import net.minecraft.core.Vec3i;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.CustomizeGuiOverlayEvent;
import net.minecraftforge.client.event.RegisterClientReloadListenersEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.level.LevelEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.IExtensionPoint;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

@Mod(Flywheel.ID)
public class Flywheel {
	public static final String ID = "flywheel";
	public static final Logger LOGGER = LoggerFactory.getLogger(ID);
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
		modEventBus.addListener(Flywheel::onCommonSetup);

		FlwConfig.get().registerSpecs(modLoadingContext);

		modLoadingContext.registerExtensionPoint(IExtensionPoint.DisplayTest.class, () -> new IExtensionPoint.DisplayTest(
				() -> "any",
				(serverVersion, isNetwork) -> true
		));

		DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> Flywheel.clientInit(forgeEventBus, modEventBus));
	}

	private static void clientInit(IEventBus forgeEventBus, IEventBus modEventBus) {
		forgeEventBus.addListener(Flywheel::addDebugInfo);

		forgeEventBus.addListener(BackendManagerImpl::onReloadLevelRenderer);

		forgeEventBus.addListener(VisualizationEventHandler::onClientTick);
		forgeEventBus.addListener(VisualizationEventHandler::onBeginFrame);
		forgeEventBus.addListener(VisualizationEventHandler::onRenderStage);
		forgeEventBus.addListener(VisualizationEventHandler::onEntityJoinLevel);
		forgeEventBus.addListener(VisualizationEventHandler::onEntityLeaveLevel);

		forgeEventBus.addListener(FlwCommands::registerClientCommands);

		forgeEventBus.addListener(Uniforms::onReloadLevelRenderer);

		forgeEventBus.addListener((LevelEvent.Unload e) -> LevelAttached.onUnloadLevel(e));

//		forgeEventBus.addListener(ExampleEffect::tick);
//		forgeEventBus.addListener(ExampleEffect::onReload);

		modEventBus.addListener(Flywheel::registerClientReloadListeners);
		modEventBus.addListener(Flywheel::onClientSetup);

		modEventBus.addListener(BackendManagerImpl::onEndClientResourceReload);

		modEventBus.addListener((EndClientResourceReloadEvent e) -> ModelCache.onEndClientResourceReload(e));
		modEventBus.addListener(MeshTree::onEndClientResourceReload);
		modEventBus.addListener(ModelHolder::onEndClientResourceReload);

		modEventBus.addListener(PartialModel::onModelRegistry);
		modEventBus.addListener(PartialModel::onModelBake);

		BackendManagerImpl.init();

		ShadersModHandler.init();

		Backends.init();
	}

	private static void registerClientReloadListeners(RegisterClientReloadListenersEvent event) {
		event.registerReloadListener(FlwPrograms.ResourceReloadListener.INSTANCE);
	}

	private static void onClientSetup(FMLClientSetupEvent event) {
		InstanceTypes.init();
		CutoutShaders.init();
		FogShaders.init();
		StandardMaterialShaders.init();

		ShaderIndices.init();

		VanillaVisuals.init();

		RegistryImpl.freezeAll();
		IdRegistryImpl.freezeAll();
	}

	private static void onCommonSetup(FMLCommonSetupEvent event) {
		// FIXME: argument types also need to be registered to BuiltInRegistries.COMMAND_ARGUMENT_TYPE
		ArgumentTypeInfos.registerByClass(BackendArgument.class, SingletonArgumentInfo.contextFree(() -> BackendArgument.INSTANCE));
	}

	private static void addDebugInfo(CustomizeGuiOverlayEvent.DebugText event) {
		Minecraft mc = Minecraft.getInstance();

		if (!mc.options.renderDebug) {
			return;
		}

		ArrayList<String> info = event.getRight();
		info.add("");
		info.add("Flywheel: " + getVersion());
		info.add("Backend: " + BackendManagerImpl.getBackendString());
		info.add("Update limiting: " + (FlwConfig.get().limitUpdates() ? "on" : "off"));

		VisualizationManager manager = VisualizationManager.get(mc.level);
		if (manager != null) {
			info.add("B: " + manager.getBlockEntities().getVisualCount()
					+ ", E: " + manager.getEntities().getVisualCount()
					+ ", F: " + manager.getEffects().getVisualCount());
			Vec3i renderOrigin = manager.getRenderOrigin();
			info.add("Origin: " + renderOrigin.getX() + ", " + renderOrigin.getY() + ", " + renderOrigin.getZ());
		}

		info.add("Memory Usage: CPU: " + StringUtil.formatBytes(FlwMemoryTracker.getCPUMemory()) + ", GPU: " + StringUtil.formatBytes(FlwMemoryTracker.getGPUMemory()));
	}

	public static ArtifactVersion getVersion() {
		return version;
	}

	public static ResourceLocation rl(String path) {
		return new ResourceLocation(ID, path);
	}
}
