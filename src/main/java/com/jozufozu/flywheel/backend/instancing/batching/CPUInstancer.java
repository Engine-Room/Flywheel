package com.jozufozu.flywheel.backend.instancing.batching;

import com.jozufozu.flywheel.api.InstanceData;
import com.jozufozu.flywheel.api.struct.BatchingTransformer;
import com.jozufozu.flywheel.api.struct.StructType;
import com.jozufozu.flywheel.backend.instancing.AbstractInstancer;
import com.jozufozu.flywheel.core.model.Model;
import com.jozufozu.flywheel.core.model.SuperByteBuffer;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.VertexFormat;

public class CPUInstancer<D extends InstanceData> extends AbstractInstancer<D> {

	private final BatchingTransformer<D> transform;

	private final SuperByteBuffer sbb;

	public CPUInstancer(StructType<D> type, Model modelData) {
		super(type, modelData);

		sbb = new SuperByteBuffer(modelData);
		transform = type.asBatched()
				.getTransformer();
	}

	@Override
	public void notifyDirty() {
		// noop
	}

	public void drawAll(PoseStack stack, VertexConsumer buffer, FormatContext context) {
		if (transform == null) {
			return;
		}

		renderSetup();

		if (context.usesOverlay()) {
			sbb.getDefaultParams().entityMode();
		}

		sbb.reset();

		for (D d : data) {
			transform.transform(d, sbb.getParams());

			sbb.renderInto(stack, buffer);
		}
	}

	protected void renderSetup() {
		if (anyToRemove) {
			removeDeletedInstances();
		}

		anyToRemove = false;
	}
}
