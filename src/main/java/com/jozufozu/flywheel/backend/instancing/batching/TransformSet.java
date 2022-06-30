package com.jozufozu.flywheel.backend.instancing.batching;

import java.util.List;

import com.jozufozu.flywheel.api.InstancedPart;
import com.jozufozu.flywheel.api.material.Material;
import com.jozufozu.flywheel.backend.instancing.TaskEngine;
import com.jozufozu.flywheel.backend.model.DirectVertexConsumer;
import com.jozufozu.flywheel.core.model.Mesh;
import com.jozufozu.flywheel.core.model.ModelTransformer;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;

public class TransformSet<D extends InstancedPart> {

	public final Material material;
	public final Mesh mesh;
	private final CPUInstancer<D> instancer;
	private final ModelTransformer modelTransformer;

	public TransformSet(CPUInstancer<D> instancer, Material material, Mesh mesh) {
		this.instancer = instancer;
		this.material = material;
		this.mesh = mesh;

		modelTransformer = new ModelTransformer(mesh);
	}

	void submitTasks(PoseStack stack, TaskEngine pool, DirectVertexConsumer consumer) {
		instancer.setup();

		int instances = instancer.getInstanceCount();

		while (instances > 0) {
			int end = instances;
			instances -= 512;
			int start = Math.max(instances, 0);

			int verts = mesh.getVertexCount() * (end - start);

			DirectVertexConsumer sub = consumer.split(verts);

			pool.submit(() -> drawRange(stack, sub, start, end));
		}
	}

	private void drawRange(PoseStack stack, VertexConsumer buffer, int from, int to) {
		drawList(stack, buffer, instancer.getRange(from, to));
	}

	void drawAll(PoseStack stack, VertexConsumer buffer) {
		drawList(stack, buffer, instancer.getAll());
	}

	private void drawList(PoseStack stack, VertexConsumer buffer, List<D> list) {
		ModelTransformer.Params params = new ModelTransformer.Params();

		for (D d : list) {
			params.loadDefault();

			instancer.type.transform(d, params);

			modelTransformer.renderInto(params, stack, buffer);
		}
	}

	public int getTotalVertexCount() {
		return mesh.getVertexCount() * instancer.getInstanceCount();
	}

	public void setOutputColorDiffuse(boolean outputColorDiffuse) {
		modelTransformer.context.outputColorDiffuse = outputColorDiffuse;
	}
}
