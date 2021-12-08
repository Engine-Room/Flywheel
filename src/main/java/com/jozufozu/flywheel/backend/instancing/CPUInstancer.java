package com.jozufozu.flywheel.backend.instancing;

import com.jozufozu.flywheel.backend.struct.BatchingTransformer;
import com.jozufozu.flywheel.backend.struct.StructType;
import com.jozufozu.flywheel.core.model.IModel;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;

public class CPUInstancer<D extends InstanceData> extends AbstractInstancer<D> {

	private final BatchingTransformer<D> renderer;

	public CPUInstancer(StructType<D> type, IModel modelData) {
		super(type, modelData);

		renderer = type.asBatched()
				.getTransformer(modelData);
	}

	public void drawAll(PoseStack stack, VertexConsumer buffer) {
		if (renderer == null) {
			return;
		}

		renderSetup();

		for (D d : data) {
			renderer.draw(d, stack, buffer);
		}
	}

	protected void renderSetup() {
		if (anyToRemove) {
			removeDeletedInstances();
		}

		anyToRemove = anyToUpdate = false;
	}
}
