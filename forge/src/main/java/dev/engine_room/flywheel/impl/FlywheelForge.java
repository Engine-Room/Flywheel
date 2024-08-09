package dev.engine_room.flywheel.impl;

import org.apache.maven.artifact.versioning.ArtifactVersion;
import org.jetbrains.annotations.UnknownNullability;

import dev.engine_room.flywheel.api.Flywheel;
import dev.engine_room.flywheel.api.event.EndClientResourceReloadEvent;
import dev.engine_room.flywheel.api.event.ReloadLevelRendererEvent;
import dev.engine_room.flywheel.backend.ForgeBackendConfig;
import dev.engine_room.flywheel.backend.LightSmoothnessArgument;
import dev.engine_room.flywheel.backend.compile.FlwProgramsReloader;
import dev.engine_room.flywheel.backend.engine.uniform.Uniforms;
import dev.engine_room.flywheel.impl.visualization.VisualizationEventHandler;
import dev.engine_room.flywheel.lib.model.ModelCache;
import dev.engine_room.flywheel.lib.model.ModelHolder;
import dev.engine_room.flywheel.lib.model.baked.PartialModelEventHandler;
import dev.engine_room.flywheel.lib.util.LevelAttached;
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
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegisterEvent;

@Mod(Flywheel.ID)
public final class FlywheelForge {
	@UnknownNullability
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
		ForgeBackendConfig.INSTANCE.registerSpecs(modLoadingContext);

		DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> FlywheelForge.clientInit(forgeEventBus, modEventBus));
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

		forgeEventBus.addListener((TickEvent.LevelTickEvent e) -> {
			// Make sure we don't tick on the server somehow.
			if (e.phase == TickEvent.Phase.END && e.side == LogicalSide.CLIENT) {
				VisualizationEventHandler.onClientTick(Minecraft.getInstance(), e.level);
			}
		});
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

		modEventBus.addListener((EndClientResourceReloadEvent e) -> BackendManagerImpl.onEndClientResourceReload(e.error().isPresent()));

		modEventBus.addListener((FMLCommonSetupEvent e) -> {
			ArgumentTypeInfos.registerByClass(BackendArgument.class, BackendArgument.INFO);
			ArgumentTypeInfos.registerByClass(DebugModeArgument.class, DebugModeArgument.INFO);
			ArgumentTypeInfos.registerByClass(LightSmoothnessArgument.class, LightSmoothnessArgument.INFO);
		});
		modEventBus.addListener((RegisterEvent e) -> {
			if (e.getRegistryKey().equals(ForgeRegistries.Keys.COMMAND_ARGUMENT_TYPES)) {
				e.register(ForgeRegistries.Keys.COMMAND_ARGUMENT_TYPES, Flywheel.rl("backend"), () -> BackendArgument.INFO);
				e.register(ForgeRegistries.Keys.COMMAND_ARGUMENT_TYPES, Flywheel.rl("debug_mode"), () -> DebugModeArgument.INFO);
				e.register(ForgeRegistries.Keys.COMMAND_ARGUMENT_TYPES, Flywheel.rl("light_smoothness"), () -> LightSmoothnessArgument.INFO);
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
