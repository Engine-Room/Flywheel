package com.jozufozu.flywheel.core;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import com.jozufozu.flywheel.Flywheel;

import net.fabricmc.fabric.api.client.model.BakedModelManagerHelper;
import net.fabricmc.fabric.api.resource.IdentifiableResourceReloadListener;
import net.fabricmc.fabric.api.resource.ResourceReloadListenerKeys;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.ModelManager;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.ResourceManagerReloadListener;

/**
 * A helper class for loading and accessing json models.
 * <br>
 * Creating a PartialModel will make the associated modelLocation automatically load.
 * PartialModels must be initialized during {@link net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent FMLClientSetupEvent}.
 * <br>
 * Once {@link ModelBakeEvent} finishes, all PartialModels (with valid modelLocations)
 * will have their bakedModel fields populated.
 * <br>
 * Attempting to create a PartialModel after ModelRegistryEvent will cause an error.
 */
public class PartialModel {

	private static final List<PartialModel> all = new ArrayList<>();

	protected final ResourceLocation modelLocation;
	protected BakedModel bakedModel;

	public PartialModel(ResourceLocation modelLocation) {

		this.modelLocation = modelLocation;
		all.add(this);
	}

	public static void onModelRegistry(ResourceManager manager, Consumer<ResourceLocation> out) {
		for (PartialModel partial : all)
			out.accept(partial.modelLocation);
	}

	public static void onModelBake(ModelManager manager) {
		for (PartialModel partial : all)
			partial.bakedModel = BakedModelManagerHelper.getModel(manager, partial.modelLocation);
	}

	public BakedModel get() {
		return bakedModel;
	}

	public static class ResourceReloadListener implements ResourceManagerReloadListener, IdentifiableResourceReloadListener {
		public static final ResourceReloadListener INSTANCE = new ResourceReloadListener();

		public static final ResourceLocation ID = Flywheel.rl("partial_models");
		public static final List<ResourceLocation> DEPENDENCIES = List.of(ResourceReloadListenerKeys.MODELS);

		@Override
		public void onResourceManagerReload(ResourceManager resourceManager) {
			onModelBake(Minecraft.getInstance().getModelManager());
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
