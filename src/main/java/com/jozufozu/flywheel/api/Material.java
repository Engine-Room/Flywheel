package com.jozufozu.flywheel.api;

import java.util.function.Supplier;

import com.jozufozu.flywheel.core.Models;
import com.jozufozu.flywheel.core.PartialModel;
import com.jozufozu.flywheel.core.model.ModelUtil;
import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.BlockState;

public interface Material<D extends InstanceData> {

	/**
	 * Get an instancer for the given model. Calling this method twice with the same key will return the same instancer.
	 *
	 * @param modelKey An object that uniquely identifies and provides the model.
	 * @return An instancer for the given model, capable of rendering many copies for little cost.
	 */
	Instancer<D> model(ModelSupplier modelKey);

	default Instancer<D> getModel(PartialModel partial) {
		return model(Models.partial(partial));
	}

	default Instancer<D> getModel(PartialModel partial, Direction dir) {
		return model(Models.partial(partial, dir, () -> ModelUtil.rotateToFace(dir)));
	}

	default Instancer<D> getModel(PartialModel partial, Direction dir, Supplier<PoseStack> modelTransform) {
		return model(Models.partial(partial, dir, modelTransform));
	}

	default Instancer<D> getModel(BlockState toRender) {
		return model(Models.block(toRender));
	}
}
