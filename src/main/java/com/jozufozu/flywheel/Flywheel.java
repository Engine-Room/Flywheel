package com.jozufozu.flywheel;

import java.util.function.Supplier;

import org.slf4j.Logger;

import com.google.common.base.Suppliers;
import com.jozufozu.flywheel.backend.Backend;
import com.jozufozu.flywheel.backend.Loader;
import com.jozufozu.flywheel.backend.RenderWork;
import com.jozufozu.flywheel.backend.ShadersModHandler;
import com.jozufozu.flywheel.backend.instancing.InstancedRenderDispatcher;
import com.jozufozu.flywheel.compat.EmbeddiumCompat;
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
import net.fabricmc.loader.api.Version;
import net.fabricmc.loader.api.metadata.version.VersionPredicate;
import net.fabricmc.loader.impl.util.version.VersionPredicateParser;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackType;

public class Flywheel implements ClientModInitializer {

	public static final String ID = "flywheel";
	public static final Logger LOGGER = LogUtils.getLogger();
	private static Version version;

	public static final Supplier<Boolean> IS_SODIUM_LOADED = Suppliers.memoize(() -> FabricLoader.getInstance().isModLoaded("sodium"));
	public static boolean isSodium0_5 = false;
	public static boolean isSodium0_6 = false;

	@Override
	public void onInitializeClient() {
		Version version = FabricLoader.getInstance()
				.getModContainer(ID)
				.orElseThrow(() -> new IllegalStateException("Could not get the mod container for Flywheel!"))
				.getMetadata()
				.getVersion();
//		if (!(version instanceof SemanticVersion semver)) {
//			throw new IllegalStateException("Got non-semantic version for Flywheel!");
//		}
		Flywheel.version = version;

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

		if (FabricLoader.getInstance().isModLoaded("embeddium")) {
			EmbeddiumCompat.init();
		}

		if (IS_SODIUM_LOADED.get()) {
			try {
				VersionPredicate predicate0_5 = VersionPredicateParser.parse(">=0.5.0 <0.6.0");
				VersionPredicate predicate0_6 = VersionPredicateParser.parse("<0.6 >=0.6.0-beta.2");
				Version sodiumVersion = FabricLoader.getInstance()
						.getModContainer("sodium")
						.orElseThrow()
						.getMetadata()
						.getVersion();
				isSodium0_5 = predicate0_5.test(sodiumVersion);
				isSodium0_6 = predicate0_6.test(sodiumVersion);
			} catch (Throwable ignored) {}
		}

		VanillaInstances.init();

		// https://github.com/Jozufozu/Flywheel/issues/69
		// Weird issue with accessor loading.
		// Only thing I've seen that's close to a fix is to force the class to load before trying to use it.
		// From the SpongePowered discord:
		// https://discord.com/channels/142425412096491520/626802111455297538/675007581168599041
		LOGGER.debug("Successfully loaded {}", PausedPartialTickAccessor.class.getName());
	}

	public static Version getVersion() {
		return version;
	}

	public static ResourceLocation rl(String path) {
		return new ResourceLocation(ID, path);
	}
}
