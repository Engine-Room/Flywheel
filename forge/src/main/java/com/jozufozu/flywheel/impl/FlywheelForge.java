package com.jozufozu.flywheel.impl;

import org.apache.maven.artifact.versioning.ArtifactVersion;

import com.jozufozu.flywheel.Flywheel;
import com.jozufozu.flywheel.api.event.BeginFrameEvent;
import com.jozufozu.flywheel.api.event.EndClientResourceReloadEvent;
import com.jozufozu.flywheel.api.event.ReloadLevelRendererEvent;
import com.jozufozu.flywheel.api.event.RenderStageEvent;
import com.jozufozu.flywheel.backend.compile.FlwProgramsReloader;
import com.jozufozu.flywheel.backend.engine.uniform.Uniforms;
import com.jozufozu.flywheel.impl.visualization.VisualizationEventHandler;
import com.jozufozu.flywheel.lib.model.ModelCache;
import com.jozufozu.flywheel.lib.model.ModelHolder;
import com.jozufozu.flywheel.lib.model.baked.PartialModelEventHandler;
import com.jozufozu.flywheel.lib.util.LevelAttached;

import net.minecraft.client.Minecraft;
import net.minecraft.commands.synchronization.ArgumentTypeInfos;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.CustomizeGuiOverlayEvent;
import net.minecraftforge.client.event.RegisterClientReloadListenersEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.event.entity.EntityLeaveLevelEvent;
import net.minecraftforge.event.level.LevelEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.CrashReportCallables;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLLoadCompleteEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegisterEvent;

@Mod(Flywheel.ID)
public final class FlywheelForge {
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

		ForgeFlwConfig.INSTANCE.registerSpecs(modLoadingContext);

		DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> FlywheelForge.clientInit(forgeEventBus, modEventBus));
	}

	private static void clientInit(IEventBus forgeEventBus, IEventBus modEventBus) {
		registerImplEventListeners(forgeEventBus, modEventBus);
		registerLibEventListeners(forgeEventBus, modEventBus);
		registerBackendEventListeners(forgeEventBus, modEventBus);

		CrashReportCallables.registerCrashCallable("Flywheel Backend", BackendManagerImpl::getBackendString);
		FlywheelInit.init();
	}

	private static void registerImplEventListeners(IEventBus forgeEventBus, IEventBus modEventBus) {
		forgeEventBus.addListener((ReloadLevelRendererEvent e) -> BackendManagerImpl.onReloadLevelRenderer(e.level()));

		forgeEventBus.addListener((TickEvent.LevelTickEvent e) -> {
			// Make sure we don't tick on the server somehow.
			if (e.phase == TickEvent.Phase.END && e.side == LogicalSide.CLIENT) {
				VisualizationEventHandler.onClientTick(Minecraft.getInstance(), e.level);
			}
		});
		forgeEventBus.addListener((BeginFrameEvent e) -> VisualizationEventHandler.onBeginFrame(e.context()));
		forgeEventBus.addListener((RenderStageEvent e) -> VisualizationEventHandler.onRenderStage(e.context(), e.stage()));
		forgeEventBus.addListener((EntityJoinLevelEvent e) -> VisualizationEventHandler.onEntityJoinLevel(e.getLevel(), e.getEntity()));
		forgeEventBus.addListener((EntityLeaveLevelEvent e) -> VisualizationEventHandler.onEntityLeaveLevel(e.getLevel(), e.getEntity()));

		forgeEventBus.addListener(FlwCommands::registerClientCommands);

		forgeEventBus.addListener((CustomizeGuiOverlayEvent.DebugText e) -> {
			Minecraft minecraft = Minecraft.getInstance();

			if (!minecraft.options.renderDebug) {
				return;
			}

			FlwDebugInfo.addDebugInfo(minecraft, e.getRight());
		});

		modEventBus.addListener((FMLLoadCompleteEvent e) -> FlywheelInit.freezeRegistries());

		modEventBus.addListener((EndClientResourceReloadEvent e) -> BackendManagerImpl.onEndClientResourceReload(e.error().isPresent()));

		modEventBus.addListener((FMLCommonSetupEvent e) -> {
			ArgumentTypeInfos.registerByClass(BackendArgument.class, BackendArgument.INFO);
		});
		modEventBus.addListener((RegisterEvent e) -> {
			if (e.getRegistryKey().equals(ForgeRegistries.Keys.COMMAND_ARGUMENT_TYPES)) {
				e.register(ForgeRegistries.Keys.COMMAND_ARGUMENT_TYPES, Flywheel.rl("backend"), () -> BackendArgument.INFO);
			}
		});
	}

	private static void registerLibEventListeners(IEventBus forgeEventBus, IEventBus modEventBus) {
		forgeEventBus.addListener((LevelEvent.Unload e) -> LevelAttached.invalidateLevel(e.getLevel()));

		modEventBus.addListener((EndClientResourceReloadEvent e) -> ModelCache.onEndClientResourceReload());
		modEventBus.addListener((EndClientResourceReloadEvent e) -> ModelHolder.onEndClientResourceReload());

		modEventBus.addListener(PartialModelEventHandler::onRegisterAdditional);
		modEventBus.addListener(PartialModelEventHandler::onBakingCompleted);
	}

	private static void registerBackendEventListeners(IEventBus forgeEventBus, IEventBus modEventBus) {
		forgeEventBus.addListener((ReloadLevelRendererEvent e) -> Uniforms.onReloadLevelRenderer());

		modEventBus.addListener((RegisterClientReloadListenersEvent e) -> {
			e.registerReloadListener(FlwProgramsReloader.INSTANCE);
		});
	}

	public static ArtifactVersion version() {
		return version;
	}
}
