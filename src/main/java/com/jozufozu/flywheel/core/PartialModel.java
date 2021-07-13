package com.jozufozu.flywheel.core;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.function.Consumer;

import com.jozufozu.flywheel.Flywheel;

import net.fabricmc.fabric.api.client.model.BakedModelManagerHelper;
import net.fabricmc.fabric.api.resource.IdentifiableResourceReloadListener;
import net.fabricmc.fabric.api.resource.ResourceReloadListenerKeys;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.model.IBakedModel;
import net.minecraft.client.renderer.model.ModelManager;
import net.minecraft.client.resources.ReloadListener;
import net.minecraft.profiler.IProfiler;
import net.minecraft.resources.IResourceManager;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Unit;

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
	protected IBakedModel bakedModel;

	public PartialModel(ResourceLocation modelLocation) {

		this.modelLocation = modelLocation;
		all.add(this);
	}

	public static void onModelRegistry(IResourceManager manager, Consumer<ResourceLocation> out) {
		for (PartialModel partial : all)
			out.accept(partial.modelLocation);
	}

	public static void onModelBake(ModelManager manager) {
		for (PartialModel partial : all)
			partial.bakedModel = BakedModelManagerHelper.getModel(manager, partial.modelLocation);
	}

	public IBakedModel get() {
		return bakedModel;
	}

	public static class ResourceReloadListener extends ReloadListener<Unit> implements IdentifiableResourceReloadListener {
		public static final ResourceReloadListener INSTANCE = new ResourceReloadListener();

		public static final ResourceLocation ID = new ResourceLocation(Flywheel.ID, "partial_models");
		public static final Collection<ResourceLocation> DEPENDENCIES = Arrays.asList(ResourceReloadListenerKeys.MODELS);

		@Override
		protected Unit prepare(IResourceManager manager, IProfiler profiler) {
			return Unit.INSTANCE;
		}

		@Override
		protected void apply(Unit unit, IResourceManager manager, IProfiler profiler) {
			onModelBake(Minecraft.getInstance().getModelManager());
		}

		@Override
		public ResourceLocation getFabricId() {
			return ID;
		}

		@Override
		public Collection<ResourceLocation> getFabricDependencies() {
			return DEPENDENCIES;
		}
	}

}
