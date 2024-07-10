package dev.engine_room.flywheel.lib.model.baked;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.ModelResourceLocation;

/**
 * A helper class for loading and accessing JSON models not directly used by any blocks or items.
 * <br>
 * Creating a PartialModel will make Minecraft automatically load the associated modelLocation.
 * PartialModels must be initialized before the initial resource reload, otherwise an error will be thrown.
 * It is recommended to do this in the client mod initializer on Fabric and the mod class constructor on Forge.
 * <br>
 * Once Minecraft has finished baking all models, all PartialModels will have their bakedModel fields populated.
 */
public class PartialModel {
	static final List<PartialModel> ALL = new ArrayList<>();
	static boolean tooLate = false;

	protected final ModelResourceLocation modelLocation;
	protected BakedModel bakedModel;

	public PartialModel(ModelResourceLocation modelLocation) {
		if (tooLate) {
			throw new RuntimeException("Attempted to create PartialModel with location '" + modelLocation + "' after start of initial resource reload!");
		}

		this.modelLocation = modelLocation;

		synchronized (ALL) {
			ALL.add(this);
		}
	}

	public ModelResourceLocation getLocation() {
		return modelLocation;
	}

	public String getName() {
		return getLocation()
				.toString();
	}

	public BakedModel get() {
		return bakedModel;
	}

	protected void set(BakedModel bakedModel) {
		this.bakedModel = bakedModel;
	}
}
