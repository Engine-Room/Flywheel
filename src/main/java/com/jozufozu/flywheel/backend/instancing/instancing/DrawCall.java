package com.jozufozu.flywheel.backend.instancing.instancing;

import com.jozufozu.flywheel.api.material.Material;
import com.jozufozu.flywheel.api.vertex.VertexType;
import com.jozufozu.flywheel.backend.gl.GlStateTracker;
import com.jozufozu.flywheel.backend.gl.array.GlVertexArray;
import com.jozufozu.flywheel.core.model.Mesh;

public class DrawCall {

	private final GPUInstancer<?> instancer;
	private final Material material;
	MeshPool.BufferedMesh bufferedMesh;
	GlVertexArray vao;

	DrawCall(GPUInstancer<?> instancer, Material material, Mesh mesh) {
		this.instancer = instancer;
		this.material = material;
		this.vao = new GlVertexArray();
		this.bufferedMesh = MeshPool.getInstance()
				.alloc(mesh);
		this.instancer.attributeBaseIndex = this.bufferedMesh.getAttributeCount();
		this.vao.enableArrays(this.bufferedMesh.getAttributeCount() + instancer.instanceFormat.getAttributeCount());
	}

	public Material getMaterial() {
		return material;
	}

	public VertexType getVertexType() {
		return bufferedMesh.getVertexType();
	}

	public void render() {
		if (invalid()) return;

		try (var ignored = GlStateTracker.getRestoreState()) {

			this.instancer.renderSetup(vao);

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

	public void delete() {
		if (invalid()) return;

		vao.delete();
		bufferedMesh.delete();

		vao = null;
		bufferedMesh = null;
	}
}
