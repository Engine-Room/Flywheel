package com.jozufozu.flywheel.backend.engine.instancing;

import com.jozufozu.flywheel.backend.InternalVertex;
import com.jozufozu.flywheel.backend.engine.InstanceHandleImpl;
import com.jozufozu.flywheel.backend.gl.array.GlVertexArray;

public class DrawCall {
	public final ShaderState shaderState;
	private final InstancedInstancer<?> instancer;
	private final InstancedMeshPool.BufferedMesh mesh;

	private GlVertexArray vao;
	private GlVertexArray vaoScratch;

	public DrawCall(InstancedInstancer<?> instancer, InstancedMeshPool.BufferedMesh mesh, ShaderState shaderState) {
		this.instancer = instancer;
		this.mesh = mesh;
		this.shaderState = shaderState;

		vao = GlVertexArray.create();
	}

	public boolean isInvalid() {
		return instancer.isInvalid() || vao == null;
	}

	public void render() {
		if (isInvalid() || mesh.isEmpty()) {
			return;
		}

		instancer.update();

		int instanceCount = instancer.getInstanceCount();
		if (instanceCount <= 0) {
			return;
		}

		instancer.bindIfNeeded(vao, InternalVertex.ATTRIBUTE_COUNT);
		mesh.setup(vao);

		vao.bindForDraw();

		mesh.draw(instanceCount);
	}

	public void renderOne(InstanceHandleImpl impl) {
		if (isInvalid() || mesh.isEmpty()) {
			return;
		}

		instancer.update();

		int instanceCount = instancer.getInstanceCount();
		if (instanceCount <= 0 || impl.index >= instanceCount) {
			return;
		}

		var vao = lazyScratchVao();

		instancer.bindRaw(vao, InternalVertex.ATTRIBUTE_COUNT, impl.index);
		mesh.setup(vao);

		vao.bindForDraw();

		mesh.draw(1);
	}

	private GlVertexArray lazyScratchVao() {
		if (vaoScratch == null) {
			vaoScratch = GlVertexArray.create();
		}
		return vaoScratch;
	}

	public void delete() {
        if (vao != null) {
            vao.delete();
            vao = null;
        }

		if (vaoScratch != null) {
			vaoScratch.delete();
			vaoScratch = null;
		}
    }
}
