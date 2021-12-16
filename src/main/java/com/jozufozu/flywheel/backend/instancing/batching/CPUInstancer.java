package com.jozufozu.flywheel.backend.instancing.batching;

import java.util.concurrent.Executor;

import com.jozufozu.flywheel.api.InstanceData;
import com.jozufozu.flywheel.api.struct.BatchingTransformer;
import com.jozufozu.flywheel.api.struct.StructType;
import com.jozufozu.flywheel.backend.instancing.AbstractInstancer;
import com.jozufozu.flywheel.backend.model.DirectVertexConsumer;
import com.jozufozu.flywheel.core.model.Model;
import com.jozufozu.flywheel.core.model.SuperByteBuffer;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;

public class CPUInstancer<D extends InstanceData> extends AbstractInstancer<D> {

	private final BatchingTransformer<D> transform;

	private final SuperByteBuffer sbb;
	private final SuperByteBuffer.Params defaultParams;

	public CPUInstancer(StructType<D> type, Model modelData) {
		super(type, modelData);

		sbb = new SuperByteBuffer(modelData);
		defaultParams = SuperByteBuffer.Params.defaultParams();
		transform = type.asBatched()
				.getTransformer();

		if (transform == null) {
			throw new NullPointerException("Cannot batch " + type.toString());
		}
	}

	void submitTasks(PoseStack stack, Executor pool, DirectVertexConsumer consumer) {
		int instances = numInstances();

		while (instances > 0) {
			int end = instances;
			instances -= 100;
			int start = Math.max(instances, 0);

			int verts = getModelVertexCount() * (end - start);

			DirectVertexConsumer sub = consumer.split(verts);

			pool.execute(() -> drawRange(stack, sub, start, end));
		}
	}

	@Override
	public void notifyDirty() {
		// noop
	}

	private void drawRange(PoseStack stack, VertexConsumer buffer, int from, int to) {
		SuperByteBuffer.Params params = defaultParams.copy();

		for (D d : data.subList(from, to)) {
			transform.transform(d, params);

			sbb.renderInto(params, stack, buffer);

			params.load(defaultParams);
		}
	}

	void drawAll(PoseStack stack, VertexConsumer buffer) {
		SuperByteBuffer.Params params = defaultParams.copy();
		for (D d : data) {
			transform.transform(d, params);

			sbb.renderInto(params, stack, buffer);

			params.load(defaultParams);
		}
	}

	void setup(FormatContext context) {
		renderSetup();

		if (context.usesOverlay()) {
			defaultParams.entityMode();
		}
	}

	protected void renderSetup() {
		if (anyToRemove) {
			removeDeletedInstances();
		}

		anyToRemove = false;
	}
}
