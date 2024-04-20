package com.jozufozu.flywheel;

import java.util.ArrayList;

import org.apache.maven.artifact.versioning.ArtifactVersion;

import com.jozufozu.flywheel.api.event.EndClientResourceReloadEvent;
import com.jozufozu.flywheel.api.event.ReloadLevelRendererEvent;
import com.jozufozu.flywheel.api.visualization.VisualizationManager;
import com.jozufozu.flywheel.backend.compile.FlwPrograms;
import com.jozufozu.flywheel.backend.engine.uniform.Uniforms;
import com.jozufozu.flywheel.config.BackendArgument;
import com.jozufozu.flywheel.config.FlwCommands;
import com.jozufozu.flywheel.config.FlwConfig;
import com.jozufozu.flywheel.config.FlwForgeConfig;
import com.jozufozu.flywheel.impl.BackendEventHandler;
import com.jozufozu.flywheel.impl.BackendManagerImpl;
import com.jozufozu.flywheel.impl.visualization.VisualizationEventHandler;
import com.jozufozu.flywheel.lib.memory.FlwMemoryTracker;
import com.jozufozu.flywheel.lib.model.ModelCache;
import com.jozufozu.flywheel.lib.model.ModelHolder;
import com.jozufozu.flywheel.lib.model.baked.PartialModelEventHandler;
import com.jozufozu.flywheel.lib.util.LevelAttached;
import com.jozufozu.flywheel.lib.util.StringUtil;

import net.minecraft.client.Minecraft;
import net.minecraft.commands.synchronization.ArgumentTypeInfos;
import net.minecraft.core.Vec3i;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.CustomizeGuiOverlayEvent;
import net.minecraftforge.client.event.RegisterClientReloadListenersEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.level.LevelEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.CrashReportCallables;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.IExtensionPoint;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLLoadCompleteEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegisterEvent;

@Mod(Flywheel.ID)
public class FlywheelForge {
	private static ArtifactVersion version;

	public FlywheelForge() {
		ModLoadingContext modLoadingContext = ModLoadingContext.get();

		version = modLoadingContext
				.getActiveContainer()
				.getModInfo()
				.getVersion();

		IEventBus forgeEventBus = MinecraftForge.EVENT_BUS;
		IEventBus modEventBus = FMLJavaModLoadingContext.get()
				.getModEventBus();
		modEventBus.addListener(FlywheelForge::onCommonSetup);
		modEventBus.addListener(FlywheelForge::onRegister);

		FlwForgeConfig.INSTANCE.registerSpecs(modLoadingContext);

		modLoadingContext.registerExtensionPoint(IExtensionPoint.DisplayTest.class, () -> new IExtensionPoint.DisplayTest(
				() -> "any",
				(serverVersion, isNetwork) -> true
		));

		DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> FlywheelForge.clientInit(forgeEventBus, modEventBus));
	}

	private static void clientInit(IEventBus forgeEventBus, IEventBus modEventBus) {
		forgeEventBus.addListener(FlywheelForge::addDebugInfo);

		forgeEventBus.addListener(BackendEventHandler::onReloadLevelRenderer);

		forgeEventBus.addListener(VisualizationEventHandler::onClientTick);
		forgeEventBus.addListener(VisualizationEventHandler::onBeginFrame);
		forgeEventBus.addListener(VisualizationEventHandler::onRenderStage);
		forgeEventBus.addListener(VisualizationEventHandler::onEntityJoinLevel);
		forgeEventBus.addListener(VisualizationEventHandler::onEntityLeaveLevel);

		forgeEventBus.addListener(FlwCommands::registerClientCommands);

		forgeEventBus.<ReloadLevelRendererEvent>addListener($ -> Uniforms.onReloadLevelRenderer());

		forgeEventBus.addListener((LevelEvent.Unload e) -> LevelAttached.invalidateLevel(e.getLevel()));

		modEventBus.addListener(FlywheelForge::registerClientReloadListeners);
		modEventBus.addListener(FlywheelForge::onClientSetup);
		modEventBus.addListener(FlywheelForge::onLoadComplete);

		modEventBus.addListener(BackendEventHandler::onEndClientResourceReload);

		modEventBus.<EndClientResourceReloadEvent>addListener($ -> ModelCache.onEndClientResourceReload());
		modEventBus.<EndClientResourceReloadEvent>addListener($ -> ModelHolder.onEndClientResourceReload());

		modEventBus.addListener(PartialModelEventHandler::onModelRegistry);
		modEventBus.addListener(PartialModelEventHandler::onModelBake);

		Flywheel.earlyInit();
		CrashReportCallables.registerCrashCallable("Flywheel Backend", BackendManagerImpl::getBackendString);
	}

	private static void registerClientReloadListeners(RegisterClientReloadListenersEvent event) {
		event.registerReloadListener(FlwPrograms.ResourceReloadListener.INSTANCE);
	}

	private static void onClientSetup(FMLClientSetupEvent event) {
		Flywheel.init();
	}

	private static void onLoadComplete(FMLLoadCompleteEvent event) {
		Flywheel.freeze();
	}

	private static void onCommonSetup(FMLCommonSetupEvent event) {
		ArgumentTypeInfos.registerByClass(BackendArgument.class, BackendArgument.INFO);
	}

	private static void onRegister(RegisterEvent event) {
		event.register(ForgeRegistries.Keys.COMMAND_ARGUMENT_TYPES, Flywheel.rl("backend"), () -> BackendArgument.INFO);
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
}
