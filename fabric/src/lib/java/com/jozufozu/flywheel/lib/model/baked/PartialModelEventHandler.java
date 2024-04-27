package com.jozufozu.flywheel.lib.model.baked;

import java.util.List;

import org.jetbrains.annotations.ApiStatus;

import com.jozufozu.flywheel.Flywheel;

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
		PartialModel.tooLate = true;
		return PartialModel.ALL.stream().map(PartialModel::getLocation).toArray(ResourceLocation[]::new);
	}

	public static void onBakingCompleted(ModelManager manager) {
		for (PartialModel partial : PartialModel.ALL) {
			partial.set(manager.getModel(partial.getLocation()));
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
