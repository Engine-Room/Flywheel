package com.jozufozu.flywheel.lib.model.baked;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.resources.ResourceLocation;

/**
 * A helper class for loading and accessing json models.
 * <br>
 * Creating a PartialModel will make the associated modelLocation automatically load.
 * PartialModels must be initialized in the mod class constructor.
 * <br>
 * Once {@link ModelEvent.RegisterAdditional} finishes, all PartialModels (with valid modelLocations)
 * will have their bakedModel fields populated.
 * <br>
 * Attempting to create a PartialModel after {@link ModelEvent.RegisterAdditional} will cause an error.
 */
public class PartialModel {
	static final List<PartialModel> ALL = new ArrayList<>();
	static boolean tooLate = false;

	protected final ResourceLocation modelLocation;
	protected BakedModel bakedModel;

	public PartialModel(ResourceLocation modelLocation) {
		if (tooLate) {
			throw new RuntimeException("PartialModel '" + modelLocation + "' loaded after ModelRegistryEvent");
		}

		this.modelLocation = modelLocation;
		ALL.add(this);
	}

	public String getName() {
		return getLocation()
				.toString();
	}

	public ResourceLocation getLocation() {
		return modelLocation;
	}

	public BakedModel get() {
		return bakedModel;
	}

	void set(BakedModel bakedModel) {
		this.bakedModel = bakedModel;
	}
}
