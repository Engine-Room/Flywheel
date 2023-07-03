package com.jozufozu.flywheel.api;

import java.util.function.Supplier;

import com.jozufozu.flywheel.core.PartialModel;
import com.jozufozu.flywheel.core.model.BlockModel;
import com.jozufozu.flywheel.core.model.Model;
import com.jozufozu.flywheel.core.model.ModelUtil;
import com.jozufozu.flywheel.util.Pair;
import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.core.Direction;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

public interface Material<D extends InstanceData> {
	/**
	 * Get an instancer for the given model. Calling this method twice with the same key will return the same instancer.
	 *
	 * @param key           An object that uniquely identifies the model.
	 * @param modelSupplier A factory that creates the Model that you want to render.
	 *                      <b>Each time {@link Supplier#get()} is called, a new Model must be created.</b>
	 * @return An instancer for the given model, capable of rendering many copies for little cost.
	 */
	Instancer<D> model(Object key, Supplier<Model> modelSupplier);

	default Instancer<D> getModel(PartialModel partial, BlockState referenceState) {
		return model(partial, () -> BlockModel.of(partial.get(), referenceState));
	}

	default Instancer<D> getModel(PartialModel partial) {
		return model(partial, () -> BlockModel.of(partial.get(), Blocks.AIR.defaultBlockState()));
	}

	default Instancer<D> getModel(PartialModel partial, BlockState referenceState, Direction dir) {
		return getModel(partial, referenceState, dir, ModelUtil.rotateToFace(dir));
	}

	default Instancer<D> getModel(PartialModel partial, BlockState referenceState, Direction dir, Supplier<PoseStack> modelTransform) {
		return model(Pair.of(dir, partial), () -> BlockModel.of(partial.get(), referenceState, modelTransform.get()));
	}

	default Instancer<D> getModel(BlockState toRender) {
		return model(toRender, () -> BlockModel.of(toRender));
	}
}
