package com.jozufozu.flywheel;

import org.slf4j.Logger;

import com.jozufozu.flywheel.backend.Backend;
import com.jozufozu.flywheel.backend.Loader;
import com.jozufozu.flywheel.backend.RenderWork;
import com.jozufozu.flywheel.backend.ShadersModHandler;
import com.jozufozu.flywheel.backend.instancing.InstancedRenderDispatcher;
import com.jozufozu.flywheel.config.FlwCommands;
import com.jozufozu.flywheel.config.FlwConfig;
import com.jozufozu.flywheel.core.Contexts;
import com.jozufozu.flywheel.core.PartialModel;
import com.jozufozu.flywheel.core.QuadConverter;
import com.jozufozu.flywheel.core.compile.ProgramCompiler;
import com.jozufozu.flywheel.core.crumbling.CrumblingRenderer;
import com.jozufozu.flywheel.event.EntityWorldHandler;
import com.jozufozu.flywheel.event.ForgeEvents;
import com.jozufozu.flywheel.fabric.event.FlywheelEvents;
import com.jozufozu.flywheel.mixin.PausedPartialTickAccessor;
import com.jozufozu.flywheel.vanilla.VanillaInstances;
import com.mojang.logging.LogUtils;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientEntityEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.model.ModelLoadingRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.SemanticVersion;
import net.fabricmc.loader.api.Version;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackType;

public class Flywheel implements ClientModInitializer {

	public static final String ID = "flywheel";
	public static final Logger LOGGER = LogUtils.getLogger();
	private static SemanticVersion version;

	@Override
	public void onInitializeClient() {
		Version version = FabricLoader.getInstance()
				.getModContainer(ID)
				.orElseThrow(() -> new IllegalStateException("Could not get the mod container for Flywheel!"))
				.getMetadata()
				.getVersion();
		if (!(version instanceof SemanticVersion semver)) {
			throw new IllegalStateException("Got non-semantic version for Flywheel!");
		}
		Flywheel.version = semver;

		FlwConfig.init();

		ShadersModHandler.init();
		Backend.init();

		ClientCommandRegistrationCallback.EVENT.register(FlwCommands::registerClientCommands);
		FlywheelEvents.RELOAD_RENDERERS.register(ProgramCompiler::invalidateAll);

		FlywheelEvents.GATHER_CONTEXT.register(Contexts::flwInit);
		ModelLoadingRegistry.INSTANCE.registerModelProvider(PartialModel::onModelRegistry);
		ResourceManagerHelper.get(PackType.CLIENT_RESOURCES).registerReloadListener(PartialModel.ResourceReloadListener.INSTANCE);

		ResourceManagerHelper.get(PackType.CLIENT_RESOURCES).registerReloadListener(Loader.ResourceReloadListener.INSTANCE);

		WorldRenderEvents.END.register(RenderWork::onRenderWorldLast);
		ClientTickEvents.END_CLIENT_TICK.register(InstancedRenderDispatcher::tick);
		FlywheelEvents.BEGIN_FRAME.register(InstancedRenderDispatcher::onBeginFrame);
		FlywheelEvents.RENDER_LAYER.register(InstancedRenderDispatcher::renderLayer);
		FlywheelEvents.RELOAD_RENDERERS.register(InstancedRenderDispatcher::onReloadRenderers);
		FlywheelEvents.RELOAD_RENDERERS.register(QuadConverter::onRendererReload);
		FlywheelEvents.RELOAD_RENDERERS.register(CrumblingRenderer::onReloadRenderers);
		ClientEntityEvents.ENTITY_LOAD.register(EntityWorldHandler::onEntityJoinWorld);
		ClientEntityEvents.ENTITY_UNLOAD.register(EntityWorldHandler::onEntityLeaveWorld);
		ClientTickEvents.END_CLIENT_TICK.register(ForgeEvents::tickLight);

		VanillaInstances.init();

		// https://github.com/Jozufozu/Flywheel/issues/69
		// Weird issue with accessor loading.
		// Only thing I've seen that's close to a fix is to force the class to load before trying to use it.
		// From the SpongePowered discord:
		// https://discord.com/channels/142425412096491520/626802111455297538/675007581168599041
		LOGGER.debug("Successfully loaded {}", PausedPartialTickAccessor.class.getName());
	}

	public static SemanticVersion getVersion() {
		return version;
	}

	public static ResourceLocation rl(String path) {
		return new ResourceLocation(ID, path);
	}
}
