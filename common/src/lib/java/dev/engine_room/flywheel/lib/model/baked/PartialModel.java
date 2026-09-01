package dev.engine_room.flywheel.lib.model.baked;

import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.UnknownNullability;

import dev.engine_room.flywheel.lib.internal.FlwLibXplat;
import net.minecraft.client.renderer.block.dispatch.BlockStateModel;
import net.minecraft.client.resources.model.SimpleModelWrapper;
import net.minecraft.resources.Identifier;

/**
 * A helper class for loading and accessing JSON models not directly used by any blocks or items.
 *
 * <p>Creating a PartialModel will make Minecraft automatically load the associated modelId and bake the model as a
 * {@link SimpleModelWrapper}.
 *
 * <p>Once Minecraft has finished baking all models, all PartialModels will have their blockStateModel fields populated.
 * Newly created PartialModels will contain a null model until the next resource reload is finished.
 */
@ApiStatus.NonExtendable
public class PartialModel {
	final Identifier modelId;
	@UnknownNullability
	BlockStateModel blockStateModel;

	PartialModel(Identifier modelId) {
		this.modelId = modelId;
	}

	public static PartialModel of(Identifier modelId) {
		return FlwLibXplat.INSTANCE.createPartialModel(modelId);
	}

	@UnknownNullability
	public BlockStateModel get() {
		return blockStateModel;
	}

	public Identifier modelId() {
		return modelId;
	}
}
