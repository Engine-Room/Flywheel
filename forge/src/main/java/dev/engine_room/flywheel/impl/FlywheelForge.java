package dev.engine_room.flywheel.impl;

import java.util.function.Supplier;

import org.apache.maven.artifact.versioning.ArtifactVersion;
import org.jetbrains.annotations.UnknownNullability;

import dev.engine_room.flywheel.api.Flywheel;
import dev.engine_room.flywheel.api.event.BeginFrameEvent;
import dev.engine_room.flywheel.api.event.EndClientResourceReloadEvent;
import dev.engine_room.flywheel.api.event.ReloadLevelRendererEvent;
import dev.engine_room.flywheel.api.event.RenderStageEvent;
import dev.engine_room.flywheel.backend.compile.FlwProgramsReloader;
import dev.engine_room.flywheel.backend.engine.uniform.Uniforms;
import dev.engine_room.flywheel.impl.visualization.VisualizationEventHandler;
import dev.engine_room.flywheel.lib.model.ModelCache;
import dev.engine_room.flywheel.lib.model.ModelHolder;
import dev.engine_room.flywheel.lib.model.baked.PartialModelEventHandler;
import dev.engine_room.flywheel.lib.util.LevelAttached;
import net.minecraft.client.Minecraft;
import net.minecraft.commands.synchronization.ArgumentTypeInfos;
import net.minecraft.core.registries.Registries;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.CrashReportCallables;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.ModLoadingContext;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.fml.loading.FMLLoader;
import net.neoforged.neoforge.client.event.CustomizeGuiOverlayEvent;
import net.neoforged.neoforge.client.event.RegisterClientReloadListenersEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;
import net.neoforged.neoforge.event.entity.EntityLeaveLevelEvent;
import net.neoforged.neoforge.event.level.LevelEvent;
import net.neoforged.neoforge.event.tick.LevelTickEvent;
import net.neoforged.neoforge.registries.RegisterEvent;

@Mod(Flywheel.ID)
public final class FlywheelForge {
	@UnknownNullability
	private static ArtifactVersion version;

	public FlywheelForge(IEventBus modEventBus, ModContainer modContainer) {
		ModLoadingContext modLoadingContext = ModLoadingContext.get();

		version = modContainer
				.getModInfo()
				.getVersion();

		IEventBus forgeEventBus = NeoForge.EVENT_BUS;

		ForgeFlwConfig.INSTANCE.registerSpecs(modContainer);

		if (FMLLoader.getDist().isClient()) {
			Supplier<Runnable> toRun = () -> () -> FlywheelForge.clientInit(forgeEventBus, modEventBus);
			toRun.get().run();
		}
	}

	private static void clientInit(IEventBus forgeEventBus, IEventBus modEventBus) {
		registerImplEventListeners(forgeEventBus, modEventBus);
		registerLibEventListeners(forgeEventBus, modEventBus);
		registerBackendEventListeners(forgeEventBus, modEventBus);

		CrashReportCallables.registerCrashCallable("Flywheel Backend", BackendManagerImpl::getBackendString);
		FlwImpl.init();
	}

	private static void registerImplEventListeners(IEventBus forgeEventBus, IEventBus modEventBus) {
		forgeEventBus.addListener((ReloadLevelRendererEvent e) -> BackendManagerImpl.onReloadLevelRenderer(e.level()));

		forgeEventBus.addListener((LevelTickEvent.Post e) -> {
			// Make sure we don't tick on the server somehow.
			if (FMLLoader.getDist().isClient()) {
				VisualizationEventHandler.onClientTick(Minecraft.getInstance(), e.getLevel());
			}
		});
		forgeEventBus.addListener((BeginFrameEvent e) -> VisualizationEventHandler.onBeginFrame(e.context()));
		forgeEventBus.addListener((RenderStageEvent e) -> VisualizationEventHandler.onRenderStage(e.context(), e.stage()));
		forgeEventBus.addListener((EntityJoinLevelEvent e) -> VisualizationEventHandler.onEntityJoinLevel(e.getLevel(), e.getEntity()));
		forgeEventBus.addListener((EntityLeaveLevelEvent e) -> VisualizationEventHandler.onEntityLeaveLevel(e.getLevel(), e.getEntity()));

		forgeEventBus.addListener(FlwCommands::registerClientCommands);

		forgeEventBus.addListener((CustomizeGuiOverlayEvent.DebugText e) -> {
			Minecraft minecraft = Minecraft.getInstance();

			if (!minecraft.getDebugOverlay().showDebugScreen()) {
				return;
			}

			FlwDebugInfo.addDebugInfo(minecraft, e.getRight());
		});

		modEventBus.addListener((EndClientResourceReloadEvent e) -> BackendManagerImpl.onEndClientResourceReload(e.error().isPresent()));

		modEventBus.addListener((FMLCommonSetupEvent e) -> {
			ArgumentTypeInfos.registerByClass(BackendArgument.class, BackendArgument.INFO);
			ArgumentTypeInfos.registerByClass(DebugModeArgument.class, DebugModeArgument.INFO);
		});
		modEventBus.addListener((RegisterEvent e) -> {
			if (e.getRegistryKey().equals(Registries.COMMAND_ARGUMENT_TYPE)) {
				e.register(Registries.COMMAND_ARGUMENT_TYPE, Flywheel.rl("backend"), () -> BackendArgument.INFO);
				e.register(Registries.COMMAND_ARGUMENT_TYPE, Flywheel.rl("debug_mode"), () -> DebugModeArgument.INFO);
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
