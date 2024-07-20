package dev.engine_room.flywheel.lib.model.baked;

import java.util.List;

import org.jetbrains.annotations.ApiStatus;

import dev.engine_room.flywheel.api.Flywheel;
import net.fabricmc.fabric.api.resource.ResourceReloadListenerKeys;
import net.fabricmc.fabric.api.resource.SimpleSynchronousResourceReloadListener;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.model.ModelManager;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;

@ApiStatus.Internal
public final class PartialModelEventHandler {
	private PartialModelEventHandler() {
	}

	public static ResourceLocation[] onRegisterAdditional() {
		return PartialModel.ALL.keySet().toArray(ResourceLocation[]::new);
	}

	public static void onBakingCompleted(ModelManager manager) {
		PartialModel.populateOnInit = true;

		for (PartialModel partial : PartialModel.ALL.values()) {
			partial.bakedModel = manager.getModel(partial.modelLocation());
		}
	}

	public static final class ReloadListener implements SimpleSynchronousResourceReloadListener {
		public static final ReloadListener INSTANCE = new ReloadListener();

		public static final ResourceLocation ID = Flywheel.rl("partial_models");
		public static final List<ResourceLocation> DEPENDENCIES = List.of(ResourceReloadListenerKeys.MODELS);

		private ReloadListener() {
		}

		@Override
		public void onResourceManagerReload(ResourceManager resourceManager) {
			onBakingCompleted(Minecraft.getInstance().getModelManager());
		}

		@Override
		public ResourceLocation getFabricId() {
			return ID;
		}

		@Override
		public List<ResourceLocation> getFabricDependencies() {
			return DEPENDENCIES;
		}
	}
}
