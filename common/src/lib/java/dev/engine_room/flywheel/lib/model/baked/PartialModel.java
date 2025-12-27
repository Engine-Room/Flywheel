package dev.engine_room.flywheel.lib.model.baked;

import java.util.concurrent.ConcurrentMap;

import org.jetbrains.annotations.UnknownNullability;

import com.google.common.collect.MapMaker;

import dev.engine_room.flywheel.lib.internal.FlwLibXplat;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.model.BlockStateModel;
import net.minecraft.resources.Identifier;

/**
 * A helper class for loading and accessing JSON models not directly used by any blocks or items.
 * <br>
 * Creating a PartialModel will make Minecraft automatically load the associated modelLocation.
 * <br>
 * Once Minecraft has finished baking all models, all PartialModels will have their blockStateModel fields populated.
 */
public final class PartialModel {
	static final ConcurrentMap<Identifier, PartialModel> ALL = new MapMaker().weakValues().makeMap();
	static boolean populateOnInit = false;

	private final Identifier modelId;
	@UnknownNullability
	BlockStateModel blockStateModel;

	private PartialModel(Identifier modelId) {
		this.modelId = modelId;

		if (populateOnInit) {
			blockStateModel = FlwLibXplat.INSTANCE.getBakedModel(Minecraft.getInstance().getModelManager(), modelId);
		}
	}

	public static PartialModel of(Identifier modelId) {
		return ALL.computeIfAbsent(modelId, PartialModel::new);
	}

	@UnknownNullability
	public BlockStateModel get() {
		return blockStateModel;
	}

	public Identifier modelId() {
		return modelId;
	}
}
