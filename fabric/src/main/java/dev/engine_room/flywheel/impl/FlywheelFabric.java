package dev.engine_room.flywheel.impl;

import org.jetbrains.annotations.UnknownNullability;

import dev.engine_room.flywheel.api.Flywheel;
import dev.engine_room.flywheel.api.event.EndClientResourceReloadCallback;
import dev.engine_room.flywheel.api.event.ReloadLevelRendererCallback;
import dev.engine_room.flywheel.backend.compile.FlwProgramsReloader;
import dev.engine_room.flywheel.backend.engine.uniform.Uniforms;
import dev.engine_room.flywheel.impl.mixin.fabric.ArgumentTypeInfosAccessor;
import dev.engine_room.flywheel.impl.task.FlwTaskExecutor;
import dev.engine_room.flywheel.impl.visualization.VisualizationEventHandler;
import dev.engine_room.flywheel.lib.model.baked.FabricPartialModel;
import dev.engine_room.flywheel.lib.util.IdentifierUtil;
import dev.engine_room.flywheel.lib.util.RendererReloadCache;
import dev.engine_room.flywheel.lib.util.ResourceReloadHolder;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientEntityEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.model.loading.v1.ModelLoadingPlugin;
import net.fabricmc.fabric.api.resource.v1.ResourceLoader;
import net.fabricmc.fabric.api.resource.v1.reloader.ResourceReloaderKeys;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.ModContainer;
import net.fabricmc.loader.api.Version;
import net.minecraft.client.gui.components.debug.DebugScreenEntries;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.server.packs.PackType;

public final class FlywheelFabric implements ClientModInitializer {
	@UnknownNullability
	private static Version version;

	@Override
	public void onInitializeClient() {
		ModContainer modContainer = FabricLoader.getInstance().getModContainer(Flywheel.ID).orElseThrow();
		version = modContainer.getMetadata().getVersion();

		setupImpl();
		setupLib();
		setupBackend();

		FlwImpl.init();
	}

	private static void setupImpl() {
		ClientLifecycleEvents.CLIENT_STOPPING.register(_ -> FlwTaskExecutor.get().shutdown());

		ReloadLevelRendererCallback.EVENT.register(BackendManagerImpl::onReloadLevelRenderer);

		// This Fabric event runs slightly later than the Forge event Flywheel uses, but it shouldn't make a difference.
		ClientTickEvents.END_CLIENT_TICK.register(minecraft -> {
			if (!minecraft.isPaused()) {
				ClientLevel level = minecraft.level;
				if (level != null) {
					VisualizationEventHandler.onClientTick(minecraft, level);
				}
			}
		});
		ClientEntityEvents.ENTITY_LOAD.register((entity, level) -> VisualizationEventHandler.onEntityJoinLevel(level, entity));
		ClientEntityEvents.ENTITY_UNLOAD.register((entity, level) -> VisualizationEventHandler.onEntityLeaveLevel(level, entity));

		ClientCommandRegistrationCallback.EVENT.register(FlwCommands::registerClientCommands);

		DebugScreenEntries.register(FlwDebugInfo.FlwDebugEntry.ID, new FlwDebugInfo.FlwDebugEntry());

		EndClientResourceReloadCallback.EVENT.register((minecraft, resourceManager, initialReload, error) ->
				BackendManagerImpl.onEndClientResourceReload(error.isPresent()));

		// We can't use ArgumentTypeRegistry from Fabric API here as it also registers to BuiltInRegistries.COMMAND_ARGUMENT_TYPE.
		// We can't register anything to BuiltInRegistries.COMMAND_ARGUMENT_TYPE because it is a synced registry but
		// Flywheel is a client-side only mod.
		ArgumentTypeInfosAccessor.getBY_CLASS().put(BackendArgument.class, BackendArgument.INFO);
		ArgumentTypeInfosAccessor.getBY_CLASS().put(DebugModeArgument.class, DebugModeArgument.INFO);
		ArgumentTypeInfosAccessor.getBY_CLASS().put(LightSmoothnessArgument.class, LightSmoothnessArgument.INFO);
	}

	private static void setupLib() {
		ReloadLevelRendererCallback.EVENT.register(level -> RendererReloadCache.onReloadLevelRenderer());
		EndClientResourceReloadCallback.EVENT.register((minecraft, resourceManager, initialReload, error) -> ResourceReloadHolder.onEndClientResourceReload());

		ModelLoadingPlugin.register(FabricPartialModel::registerAll);
		ResourceLoader.get(PackType.CLIENT_RESOURCES).registerReloadListener(FabricPartialModel.ResourceReloadListener.ID, FabricPartialModel.ResourceReloadListener.INSTANCE);
		ResourceLoader.get(PackType.CLIENT_RESOURCES).addListenerOrdering(ResourceReloaderKeys.Client.MODELS, FabricPartialModel.ResourceReloadListener.ID);
	}

	private static void setupBackend() {
		ReloadLevelRendererCallback.EVENT.register(level -> Uniforms.onReloadLevelRenderer());

		ResourceLoader.get(PackType.CLIENT_RESOURCES).registerReloadListener(FlwProgramsReloader.ID, FlwProgramsReloader.INSTANCE);
	}

	public static Version version() {
		return version;
	}
}
