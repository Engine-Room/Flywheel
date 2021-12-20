package com.jozufozu.flywheel;

import com.jozufozu.flywheel.backend.Backend;
import com.jozufozu.flywheel.backend.Loader;
import com.jozufozu.flywheel.backend.RenderWork;
import com.jozufozu.flywheel.backend.instancing.InstancedRenderDispatcher;
import com.jozufozu.flywheel.config.FlwConfig;
import com.jozufozu.flywheel.core.Contexts;
import com.jozufozu.flywheel.core.Materials;
import com.jozufozu.flywheel.core.PartialModel;
import com.jozufozu.flywheel.core.QuadConverter;
import com.jozufozu.flywheel.core.crumbling.CrumblingRenderer;
import com.jozufozu.flywheel.event.EntityWorldHandler;
import com.jozufozu.flywheel.event.ForgeEvents;
import com.jozufozu.flywheel.fabric.event.FlywheelEvents;
import com.jozufozu.flywheel.mixin.PausedPartialTickAccessor;
import com.jozufozu.flywheel.vanilla.VanillaInstances;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientEntityEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.model.ModelLoadingRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.minecraft.server.packs.PackType;

public class FlywheelClient implements ClientModInitializer {

	@Override
	public void onInitializeClient() {

		Backend.init();

		FlywheelEvents.GATHER_CONTEXT.register(Contexts::flwInit);
		FlywheelEvents.GATHER_CONTEXT.register(Materials::flwInit);
		ModelLoadingRegistry.INSTANCE.registerModelProvider(PartialModel::onModelRegistry);
		ResourceManagerHelper.get(PackType.CLIENT_RESOURCES).registerReloadListener(PartialModel.ResourceReloadListener.INSTANCE);

		VanillaInstances.init();

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

		FlwConfig.init();

		// https://github.com/Jozufozu/Flywheel/issues/69
		// Weird issue with accessor loading.
		// Only thing I've seen that's close to a fix is to force the class to load before trying to use it.
		// From the SpongePowered discord:
		// https://discord.com/channels/142425412096491520/626802111455297538/675007581168599041
		Flywheel.log.info("Successfully loaded {}", PausedPartialTickAccessor.class.getName());
	}
}
