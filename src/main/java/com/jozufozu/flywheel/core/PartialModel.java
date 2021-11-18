package com.jozufozu.flywheel.core;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.client.event.ModelBakeEvent;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.client.model.ModelLoader;

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

	private static final List<PartialModel> ALL = new ArrayList<>();
	private static boolean tooLate = false;

	protected final ResourceLocation modelLocation;
	protected BakedModel bakedModel;

	public PartialModel(ResourceLocation modelLocation) {
		if (tooLate) throw new RuntimeException("PartialModel '" + modelLocation + "' loaded after ModelRegistryEvent");

		this.modelLocation = modelLocation;
		ALL.add(this);
	}

	public static void onModelRegistry(ModelRegistryEvent event) {
		for (PartialModel partial : ALL)
			ModelLoader.addSpecialModel(partial.getLocation());

		tooLate = true;
	}

	public static void onModelBake(ModelBakeEvent event) {
		Map<ResourceLocation, BakedModel> modelRegistry = event.getModelRegistry();
		for (PartialModel partial : ALL)
			partial.set(modelRegistry.get(partial.getLocation()));
	}

	protected void set(BakedModel bakedModel) {
		this.bakedModel = bakedModel;
	}

	public ResourceLocation getLocation() {
		return modelLocation;
	}

	public BakedModel get() {
		return bakedModel;
	}

}
