package com.jozufozu.flywheel.backend.engine.instancing;

import com.jozufozu.flywheel.gl.array.GlVertexArray;

public class DrawCall {
	private final InstancedInstancer<?> instancer;
	private final InstancedMeshPool.BufferedMesh mesh;

	private final int meshAttributes;
	private GlVertexArray vao;

	public DrawCall(InstancedInstancer<?> instancer, InstancedMeshPool.BufferedMesh mesh) {
		this.instancer = instancer;
		this.mesh = mesh;

		meshAttributes = this.mesh.getAttributeCount();
		vao = GlVertexArray.create();
	}

	public boolean isInvalid() {
		return instancer.isInvalid() || vao == null;
	}

	public void render() {
		if (isInvalid()) {
			return;
		}

		instancer.update();

		int instanceCount = instancer.getInstanceCount();
		if (instanceCount <= 0 || mesh.isEmpty()) {
			return;
		}

		instancer.bindToVAO(vao, meshAttributes);
		mesh.setup(vao);

		vao.bindForDraw();

		mesh.draw(instanceCount);
	}

	public void delete() {
		if (vao == null) {
			return;
		}

		vao.delete();
		vao = null;
	}
}
