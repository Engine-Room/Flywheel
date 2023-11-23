package com.jozufozu.flywheel;

import java.util.ArrayList;

import org.apache.maven.artifact.versioning.ArtifactVersion;
import org.slf4j.Logger;

import com.jozufozu.flywheel.api.event.ReloadRenderersEvent;
import com.jozufozu.flywheel.api.visualization.VisualizationManager;
import com.jozufozu.flywheel.backend.Backends;
import com.jozufozu.flywheel.backend.Loader;
import com.jozufozu.flywheel.backend.compile.Pipelines;
import com.jozufozu.flywheel.backend.engine.UniformBuffer;
import com.jozufozu.flywheel.backend.engine.batching.DrawBuffer;
import com.jozufozu.flywheel.config.BackendArgument;
import com.jozufozu.flywheel.config.FlwCommands;
import com.jozufozu.flywheel.config.FlwConfig;
import com.jozufozu.flywheel.impl.BackendManagerImpl;
import com.jozufozu.flywheel.impl.IdRegistryImpl;
import com.jozufozu.flywheel.impl.RegistryImpl;
import com.jozufozu.flywheel.impl.visualization.VisualizationEventHandler;
import com.jozufozu.flywheel.lib.context.Contexts;
import com.jozufozu.flywheel.lib.instance.InstanceTypes;
import com.jozufozu.flywheel.lib.light.LightUpdater;
import com.jozufozu.flywheel.lib.material.MaterialIndices;
import com.jozufozu.flywheel.lib.material.Materials;
import com.jozufozu.flywheel.lib.memory.FlwMemoryTracker;
import com.jozufozu.flywheel.lib.model.ModelCache;
import com.jozufozu.flywheel.lib.model.ModelHolder;
import com.jozufozu.flywheel.lib.model.baked.PartialModel;
import com.jozufozu.flywheel.lib.util.LevelAttached;
import com.jozufozu.flywheel.lib.util.ShadersModHandler;
import com.jozufozu.flywheel.lib.util.StringUtil;
import com.jozufozu.flywheel.lib.vertex.VertexTypes;
import com.jozufozu.flywheel.vanilla.VanillaVisuals;
import com.mojang.logging.LogUtils;

import net.minecraft.client.Minecraft;
import net.minecraft.core.Vec3i;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.level.LevelEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.IExtensionPoint;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

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

		FlwConfig.get().registerSpecs(modLoadingContext);

		modLoadingContext.registerExtensionPoint(IExtensionPoint.DisplayTest.class, () -> new IExtensionPoint.DisplayTest(
				() -> NetworkConstants.IGNORESERVERONLY,
				(serverVersion, isNetwork) -> isNetwork
		));

		DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> Flywheel.clientInit(forgeEventBus, modEventBus));
	}

	private static void clientInit(IEventBus forgeEventBus, IEventBus modEventBus) {
		forgeEventBus.addListener(Flywheel::addDebugInfo);

		forgeEventBus.addListener(BackendManagerImpl::onReloadRenderers);

		forgeEventBus.addListener(VisualizationEventHandler::onClientTick);
		forgeEventBus.addListener(VisualizationEventHandler::onBeginFrame);
		forgeEventBus.addListener(VisualizationEventHandler::onRenderStage);
		forgeEventBus.addListener(VisualizationEventHandler::onEntityJoinWorld);
		forgeEventBus.addListener(VisualizationEventHandler::onEntityLeaveWorld);

		forgeEventBus.addListener(FlwCommands::registerClientCommands);

		forgeEventBus.addListener(DrawBuffer::onReloadRenderers);
		forgeEventBus.addListener(UniformBuffer::onReloadRenderers);

		forgeEventBus.addListener(LightUpdater::onClientTick);
		forgeEventBus.addListener((ReloadRenderersEvent e) -> ModelCache.onReloadRenderers(e));
		forgeEventBus.addListener(ModelHolder::onReloadRenderers);
		forgeEventBus.addListener((LevelEvent.Unload e) -> LevelAttached.onUnloadLevel(e));

		modEventBus.addListener(PartialModel::onModelRegistry);
		modEventBus.addListener(PartialModel::onModelBake);

//		forgeEventBus.addListener(ExampleEffect::tick);
//		forgeEventBus.addListener(ExampleEffect::onReload);

		BackendManagerImpl.init();

		Pipelines.init();
		Backends.init();
		Loader.init();

		ShadersModHandler.init();

		VertexTypes.init();
		InstanceTypes.init();
		Materials.init();
		Contexts.init();

		MaterialIndices.init();

		VanillaVisuals.init();
	}

	private static void setup(final FMLCommonSetupEvent event) {
		RegistryImpl.freezeAll();
		IdRegistryImpl.freezeAll();

		ArgumentTypes.register(rl("backend").toString(), BackendArgument.class, new EmptyArgumentSerializer<>(() -> BackendArgument.INSTANCE));
	}

	private static void addDebugInfo(RenderGameOverlayEvent.Text event) {
		Minecraft mc = Minecraft.getInstance();
		if (!mc.options.renderDebug) {
			return;
		}

		ArrayList<String> info = event.getRight();
		info.add("");
		info.add("Flywheel: " + getVersion());
		info.add("Backend: " + BackendManagerImpl.getBackendString());
		info.add("Update limiting: " + FlwCommands.boolToText(FlwConfig.get().limitUpdates()).getString());

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
