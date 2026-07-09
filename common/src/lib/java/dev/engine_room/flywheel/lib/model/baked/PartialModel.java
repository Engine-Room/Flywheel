package dev.engine_room.flywheel.lib.model.baked;

import java.util.concurrent.ConcurrentMap;

import org.jetbrains.annotations.UnknownNullability;

import com.google.common.collect.MapMaker;

import dev.engine_room.flywheel.lib.internal.FlwLibXplat;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.dispatch.BlockStateModel;
import net.minecraft.resources.Identifier;

/**
 * A helper class for loading and accessing JSON models not directly used by any blocks or items.
 * <br>
 * Creating a PartialModel will make Minecraft automatically load the associated modelLocation.
 * <br>
 * Once Minecraft has finished baking all models, all PartialModels will have their bakedModel fields populated.
 */
public final class PartialModel {
	static final ConcurrentMap<Identifier, PartialModel> ALL = new MapMaker().weakValues().makeMap();
	static boolean populateOnInit = false;

	private final Identifier modelLocation;
	@UnknownNullability
	BlockStateModel bakedModel;

	private PartialModel(Identifier modelLocation) {
		this.modelLocation = modelLocation;

		if (populateOnInit) {
			bakedModel = FlwLibXplat.INSTANCE.getBakedModel(Minecraft.getInstance().getModelManager(), modelLocation);
		}
	}

	public static PartialModel of(Identifier modelLocation) {
		return ALL.computeIfAbsent(modelLocation, PartialModel::new);
	}

	@UnknownNullability
	public BlockStateModel get() {
		return bakedModel;
	}

	public Identifier modelLocation() {
		return modelLocation;
	}
}
