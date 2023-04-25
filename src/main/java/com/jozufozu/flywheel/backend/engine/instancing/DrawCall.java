package com.jozufozu.flywheel.backend.engine.instancing;

import com.jozufozu.flywheel.gl.GlStateTracker;
import com.jozufozu.flywheel.gl.array.GlVertexArray;

public class DrawCall {
	private final GPUInstancer<?> instancer;
	private final InstancedMeshPool.BufferedMesh mesh;

	private final int meshAttributes;
	private GlVertexArray vao;

	public DrawCall(GPUInstancer<?> instancer, InstancedMeshPool.BufferedMesh mesh) {
		this.instancer = instancer;
		this.mesh = mesh;

		meshAttributes = this.mesh.getAttributeCount();
		vao = new GlVertexArray();
	}

	public boolean isInvalid() {
		return instancer.isInvalid() || vao == null;
	}

	public void render() {
		if (isInvalid()) {
			return;
		}

		try (var ignored = GlStateTracker.getRestoreState()) {
			instancer.update();

			instancer.bindToVAO(vao, meshAttributes);

			if (instancer.getInstanceCount() > 0) {
				mesh.drawInstances(vao, instancer.getInstanceCount());
			}
		}
	}

	public void delete() {
		if (vao == null) {
			return;
		}

		vao.delete();
		vao = null;
	}
}
