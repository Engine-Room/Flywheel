package dev.engine_room.flywheel.lib.model.baked;

import java.util.concurrent.ConcurrentMap;

import org.jetbrains.annotations.UnknownNullability;

import com.google.common.collect.MapMaker;

import dev.engine_room.flywheel.lib.internal.FlwLibXplat;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.resources.ResourceLocation;

/**
 * A helper class for loading and accessing JSON models not directly used by any blocks or items.
 * <br>
 * Creating a PartialModel will make Minecraft automatically load the associated modelLocation.
 * <br>
 * Once Minecraft has finished baking all models, all PartialModels will have their bakedModel fields populated.
 */
public final class PartialModel {
	static final ConcurrentMap<ResourceLocation, PartialModel> ALL = new MapMaker().weakValues().makeMap();
	static boolean populateOnInit = false;

	private final ResourceLocation modelLocation;
	@UnknownNullability
	BakedModel bakedModel;

	private PartialModel(ResourceLocation modelLocation) {
		this.modelLocation = modelLocation;

		if (populateOnInit) {
			bakedModel = FlwLibXplat.INSTANCE.getBakedModel(Minecraft.getInstance().getModelManager(), modelLocation);
		}
	}

	public static PartialModel of(ResourceLocation modelLocation) {
		return ALL.computeIfAbsent(modelLocation, PartialModel::new);
	}

	@UnknownNullability
	public BakedModel get() {
		return bakedModel;
	}

	public ResourceLocation modelLocation() {
		return modelLocation;
	}
}
