package com.jozufozu.flywheel;

import com.jozufozu.flywheel.backend.Backend;
import com.jozufozu.flywheel.backend.RenderWork;
import com.jozufozu.flywheel.backend.ShaderSources;
import com.jozufozu.flywheel.backend.instancing.CrumblingRenderer;
import com.jozufozu.flywheel.backend.instancing.InstancedRenderDispatcher;
import com.jozufozu.flywheel.config.FlwConfig;
import com.jozufozu.flywheel.core.AtlasStitcher;
import com.jozufozu.flywheel.core.Contexts;
import com.jozufozu.flywheel.core.Materials;
import com.jozufozu.flywheel.core.PartialModel;
import com.jozufozu.flywheel.core.QuadConverter;
import com.jozufozu.flywheel.event.EntityWorldHandler;
import com.jozufozu.flywheel.fabric.event.FlywheelEvents;
import com.jozufozu.flywheel.vanilla.VanillaInstances;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientEntityEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.model.ModelLoadingRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.fabricmc.fabric.api.event.client.ClientSpriteRegistryCallback;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.minecraft.inventory.container.PlayerContainer;
import net.minecraft.resources.ResourcePackType;

public class FlywheelClient implements ClientModInitializer {

	@Override
	public void onInitializeClient() {

		Backend.init();

		ClientSpriteRegistryCallback.event(PlayerContainer.BLOCK_ATLAS_TEXTURE).register(AtlasStitcher.getInstance()::onTextureStitch);

		FlywheelEvents.GATHER_CONTEXT.register(Contexts::flwInit);
		FlywheelEvents.GATHER_CONTEXT.register(Materials::flwInit);
		ModelLoadingRegistry.INSTANCE.registerModelProvider(PartialModel::onModelRegistry);
		ResourceManagerHelper.get(ResourcePackType.CLIENT_RESOURCES).registerReloadListener(PartialModel.ResourceReloadListener.INSTANCE);

		VanillaInstances.init();

		ResourceManagerHelper.get(ResourcePackType.CLIENT_RESOURCES).registerReloadListener(ShaderSources.ResourceReloadListener.INSTANCE);

		WorldRenderEvents.END.register(RenderWork::onRenderWorldLast);
		FlywheelEvents.RELOAD_RENDERERS.register(CrumblingRenderer::onReloadRenderers);
		ClientTickEvents.END_CLIENT_TICK.register(InstancedRenderDispatcher::tick);
		FlywheelEvents.BEGIN_FRAME.register(InstancedRenderDispatcher::onBeginFrame);
		FlywheelEvents.RENDER_LAYER.register(InstancedRenderDispatcher::renderLayer);
		FlywheelEvents.RELOAD_RENDERERS.register(InstancedRenderDispatcher::onReloadRenderers);
		FlywheelEvents.RELOAD_RENDERERS.register(QuadConverter::onRendererReload);
		ClientEntityEvents.ENTITY_LOAD.register(EntityWorldHandler::onEntityJoinWorld);
		ClientEntityEvents.ENTITY_UNLOAD.register(EntityWorldHandler::onEntityLeaveWorld);

		FlwConfig.init();
	}
}
