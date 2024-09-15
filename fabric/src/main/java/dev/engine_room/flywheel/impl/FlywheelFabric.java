package dev.engine_room.flywheel.impl;

import org.jetbrains.annotations.UnknownNullability;

import dev.engine_room.flywheel.api.Flywheel;
import dev.engine_room.flywheel.api.event.EndClientResourceReloadCallback;
import dev.engine_room.flywheel.api.event.ReloadLevelRendererCallback;
import dev.engine_room.flywheel.backend.LightSmoothnessArgument;
import dev.engine_room.flywheel.backend.compile.FlwProgramsReloader;
import dev.engine_room.flywheel.backend.engine.uniform.Uniforms;
import dev.engine_room.flywheel.impl.visualization.VisualizationEventHandler;
import dev.engine_room.flywheel.lib.model.ResourceReloadCache;
import dev.engine_room.flywheel.lib.model.ResourceReloadHolder;
import dev.engine_room.flywheel.lib.model.baked.PartialModelEventHandler;
import dev.engine_room.flywheel.lib.model.part.MeshTree;
import dev.engine_room.flywheel.lib.model.part.ModelTree;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientEntityEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.model.loading.v1.ModelLoadingPlugin;
import net.fabricmc.fabric.api.command.v2.ArgumentTypeRegistry;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.ModContainer;
import net.fabricmc.loader.api.Version;
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

		EndClientResourceReloadCallback.EVENT.register((minecraft, resourceManager, initialReload, error) ->
				BackendManagerImpl.onEndClientResourceReload(error.isPresent()));

		ArgumentTypeRegistry.registerArgumentType(Flywheel.rl("backend"), BackendArgument.class, BackendArgument.INFO);
		ArgumentTypeRegistry.registerArgumentType(Flywheel.rl("debug_mode"), DebugModeArgument.class, DebugModeArgument.INFO);
		ArgumentTypeRegistry.registerArgumentType(Flywheel.rl("light_smoothness"), LightSmoothnessArgument.class, LightSmoothnessArgument.INFO);
	}

	private static void setupLib() {
		EndClientResourceReloadCallback.EVENT.register((minecraft, resourceManager, initialReload, error) -> ResourceReloadCache.onEndClientResourceReload());
		EndClientResourceReloadCallback.EVENT.register((minecraft, resourceManager, initialReload, error) -> ResourceReloadHolder.onEndClientResourceReload());
		EndClientResourceReloadCallback.EVENT.register((minecraft, resourceManager, initialReload, error) ->
				MeshTree.onEndClientResourceReload());
		EndClientResourceReloadCallback.EVENT.register((minecraft, resourceManager, initialReload, error) -> ModelTree.onEndClientResourceReload());

		ModelLoadingPlugin.register(ctx -> {
			ctx.addModels(PartialModelEventHandler.onRegisterAdditional());
		});
		ResourceManagerHelper.get(PackType.CLIENT_RESOURCES).registerReloadListener(PartialModelEventHandler.ReloadListener.INSTANCE);
	}

	private static void setupBackend() {
		ReloadLevelRendererCallback.EVENT.register(level -> Uniforms.onReloadLevelRenderer());

		ResourceManagerHelper.get(PackType.CLIENT_RESOURCES).registerReloadListener(FlwProgramsReloader.INSTANCE);
	}

	public static Version version() {
		return version;
	}
}
