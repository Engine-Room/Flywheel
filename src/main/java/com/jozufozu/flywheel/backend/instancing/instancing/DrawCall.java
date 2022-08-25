package com.jozufozu.flywheel.backend.instancing.instancing;

import com.jozufozu.flywheel.api.material.Material;
import com.jozufozu.flywheel.api.vertex.VertexType;
import com.jozufozu.flywheel.backend.gl.GlStateTracker;
import com.jozufozu.flywheel.backend.gl.array.GlVertexArray;

public class DrawCall {

	final GPUInstancer<?> instancer;
	final Material material;
	private final int meshAttributes;
	InstancedMeshPool.BufferedMesh bufferedMesh;
	GlVertexArray vao;

	DrawCall(GPUInstancer<?> instancer, Material material, InstancedMeshPool.BufferedMesh mesh) {
		this.instancer = instancer;
		this.material = material;
		this.vao = new GlVertexArray();
		this.bufferedMesh = mesh;
		this.meshAttributes = this.bufferedMesh.getAttributeCount();
		this.vao.enableArrays(this.meshAttributes + instancer.instanceFormat.getAttributeCount());
	}

	public Material getMaterial() {
		return material;
	}

	public VertexType getVertexType() {
		return bufferedMesh.getVertexType();
	}

	public void render() {
		if (invalid()) {
			return;
		}

		try (var ignored = GlStateTracker.getRestoreState()) {

			this.instancer.update();

			bindInstancerToVAO();

			if (this.instancer.glInstanceCount > 0) {
				bufferedMesh.drawInstances(vao, this.instancer.glInstanceCount);
			}
		}
	}

	public boolean shouldRemove() {
		return invalid();
	}

	/**
	 * Only {@code true} if the InstancedModel has been destroyed.
	 */
	private boolean invalid() {
		return this.instancer.vbo == null || bufferedMesh == null || vao == null;
	}

	private void bindInstancerToVAO() {
		if (!this.instancer.boundTo.add(vao)) {
			return;
		}

		var instanceFormat = this.instancer.instanceFormat;

		vao.bindAttributes(this.instancer.vbo, this.meshAttributes, instanceFormat, 0L);

		for (int i = 0; i < instanceFormat.getAttributeCount(); i++) {
			vao.setAttributeDivisor(this.meshAttributes + i, 1);
		}
	}

	public void delete() {
		if (invalid()) {
			return;
		}

		vao.delete();
		bufferedMesh.delete();

		vao = null;
		bufferedMesh = null;
	}
}
