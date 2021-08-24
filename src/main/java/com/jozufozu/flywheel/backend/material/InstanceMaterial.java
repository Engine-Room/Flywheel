package com.jozufozu.flywheel.backend.material;

import java.util.function.Supplier;

import com.jozufozu.flywheel.backend.instancing.InstanceData;
import com.jozufozu.flywheel.backend.instancing.Instancer;
import com.jozufozu.flywheel.core.Formats;
import com.jozufozu.flywheel.core.PartialModel;
import com.jozufozu.flywheel.core.model.BlockModel;
import com.jozufozu.flywheel.core.model.IModel;
import com.jozufozu.flywheel.util.Pair;
import com.jozufozu.flywheel.util.RenderUtil;
import com.mojang.blaze3d.matrix.MatrixStack;

import net.minecraft.block.BlockState;
import net.minecraft.util.Direction;

public interface InstanceMaterial<D extends InstanceData> {
	/**
	 * Get an instancer for the given model. Calling this method twice with the same key will return the same instancer.
	 *
	 * @param key           An object that uniquely identifies the model.
	 * @param modelSupplier A factory that creates the IModel that you want to render.
	 * @return An instancer for the given model, capable of rendering many copies for little cost.
	 */
	Instancer<D> model(Object key, Supplier<IModel> modelSupplier);

	default Instancer<D> getModel(PartialModel partial, BlockState referenceState) {
		return model(partial, () -> new BlockModel(Formats.UNLIT_MODEL, partial.get(), referenceState));
	}

	default Instancer<D> getModel(PartialModel partial, BlockState referenceState, Direction dir) {
		return getModel(partial, referenceState, dir, RenderUtil.rotateToFace(dir));
	}

	default Instancer<D> getModel(PartialModel partial, BlockState referenceState, Direction dir, Supplier<MatrixStack> modelTransform) {
		return model(Pair.of(dir, partial), () -> new BlockModel(Formats.UNLIT_MODEL, partial.get(), referenceState, modelTransform.get()));
	}

	default Instancer<D> getModel(BlockState toRender) {
		return model(toRender, () -> new BlockModel(Formats.UNLIT_MODEL, toRender));
	}
}
