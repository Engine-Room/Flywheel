package dev.engine_room.flywheel.impl;

import org.apache.maven.artifact.versioning.ArtifactVersion;
import org.jetbrains.annotations.UnknownNullability;

import dev.engine_room.flywheel.api.Flywheel;
import dev.engine_room.flywheel.api.event.EndClientResourceReloadEvent;
import dev.engine_room.flywheel.api.event.ReloadLevelRendererEvent;
import dev.engine_room.flywheel.backend.compile.FlwProgramsReloader;
import dev.engine_room.flywheel.backend.engine.uniform.Uniforms;
import dev.engine_room.flywheel.impl.task.FlwTaskExecutor;
import dev.engine_room.flywheel.impl.visualization.VisualizationEventHandler;
import dev.engine_room.flywheel.lib.model.baked.NeoForgePartialModel;
import dev.engine_room.flywheel.lib.util.IdentifierUtil;
import dev.engine_room.flywheel.lib.util.LevelAttached;
import dev.engine_room.flywheel.lib.util.RendererReloadCache;
import dev.engine_room.flywheel.lib.util.ResourceReloadHolder;
import net.minecraft.client.Minecraft;
import net.minecraft.commands.synchronization.ArgumentTypeInfos;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.CrashReportCallables;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.client.event.AddClientReloadListenersEvent;
import net.neoforged.neoforge.client.event.RegisterDebugEntriesEvent;
import net.neoforged.neoforge.client.event.lifecycle.ClientStoppingEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;
import net.neoforged.neoforge.event.entity.EntityLeaveLevelEvent;
import net.neoforged.neoforge.event.level.LevelEvent;
import net.neoforged.neoforge.event.tick.LevelTickEvent;

@Mod(value = Flywheel.ID, dist = Dist.CLIENT)
public final class FlywheelNeoForge {
	@UnknownNullability
	private static ArtifactVersion version;

	public FlywheelNeoForge(IEventBus modEventBus, ModContainer modContainer) {
		version = modContainer
				.getModInfo()
				.getVersion();

		IEventBus gameEventBus = NeoForge.EVENT_BUS;

		NeoForgeFlwConfig.INSTANCE.registerSpecs(modContainer);

		registerImplEventListeners(gameEventBus, modEventBus);
		registerLibEventListeners(gameEventBus, modEventBus);
		registerBackendEventListeners(gameEventBus, modEventBus);

		CrashReportCallables.registerCrashCallable("Flywheel Backend", BackendManagerImpl::getBackendString);
		FlwImpl.init();
	}

	private static void registerImplEventListeners(IEventBus gameEventBus, IEventBus modEventBus) {
		gameEventBus.addListener((ClientStoppingEvent e) -> FlwTaskExecutor.get().shutdown());

		gameEventBus.addListener((ReloadLevelRendererEvent e) -> BackendManagerImpl.onReloadLevelRenderer(e.level()));

		gameEventBus.addListener((LevelTickEvent.Post e) -> {
			// Make sure we don't tick on the server somehow.
			if (e.getLevel().isClientSide()) {
				VisualizationEventHandler.onClientTick(Minecraft.getInstance(), e.getLevel());
			}
		});
		gameEventBus.addListener((EntityJoinLevelEvent e) -> VisualizationEventHandler.onEntityJoinLevel(e.getLevel(), e.getEntity()));
		gameEventBus.addListener((EntityLeaveLevelEvent e) -> VisualizationEventHandler.onEntityLeaveLevel(e.getLevel(), e.getEntity()));

		gameEventBus.addListener(FlwCommands::registerClientCommands);

		modEventBus.addListener((RegisterDebugEntriesEvent e) -> e.register(FlwDebugInfo.FlwDebugEntry.ID, new FlwDebugInfo.FlwDebugEntry()));

		modEventBus.addListener((EndClientResourceReloadEvent e) -> BackendManagerImpl.onEndClientResourceReload(e.error().isPresent()));

		modEventBus.addListener((FMLCommonSetupEvent e) -> {
			// We can't register anything to Registries.COMMAND_ARGUMENT_TYPE because it is a synced registry but
			// Flywheel is a client-side only mod.
			ArgumentTypeInfos.registerByClass(BackendArgument.class, BackendArgument.INFO);
			ArgumentTypeInfos.registerByClass(DebugModeArgument.class, DebugModeArgument.INFO);
			ArgumentTypeInfos.registerByClass(LightSmoothnessArgument.class, LightSmoothnessArgument.INFO);
		});
	}

	private static void registerLibEventListeners(IEventBus gameEventBus, IEventBus modEventBus) {
		gameEventBus.addListener((LevelEvent.Unload e) -> LevelAttached.invalidateLevel(e.getLevel()));

		modEventBus.addListener((EndClientResourceReloadEvent e) -> RendererReloadCache.onReloadLevelRenderer());
		modEventBus.addListener((EndClientResourceReloadEvent e) -> ResourceReloadHolder.onEndClientResourceReload());

		modEventBus.addListener(NeoForgePartialModel::registerAll);
		modEventBus.addListener(NeoForgePartialModel::refreshAll);
	}

	private static void registerBackendEventListeners(IEventBus gameEventBus, IEventBus modEventBus) {
		gameEventBus.addListener((ReloadLevelRendererEvent e) -> Uniforms.onReloadLevelRenderer());

		modEventBus.addListener((AddClientReloadListenersEvent e) -> {
			e.addListener(FlwProgramsReloader.ID, FlwProgramsReloader.INSTANCE);
		});
	}

	public static ArtifactVersion version() {
		return version;
	}
}
