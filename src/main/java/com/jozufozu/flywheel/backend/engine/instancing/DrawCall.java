package com.jozufozu.flywheel.backend.engine.instancing;

import org.jetbrains.annotations.Nullable;

import com.jozufozu.flywheel.backend.InternalVertex;
import com.jozufozu.flywheel.backend.engine.InstanceHandleImpl;
import com.jozufozu.flywheel.backend.engine.MeshPool;
import com.jozufozu.flywheel.backend.gl.array.GlVertexArray;

public class DrawCall {
	public final ShaderState shaderState;
	private final InstancedInstancer<?> instancer;
	private final MeshPool.BufferedMesh mesh;

	private final GlVertexArray vao;
	@Nullable
	private GlVertexArray vaoScratch;
	private boolean deleted;

	public DrawCall(InstancedInstancer<?> instancer, MeshPool.BufferedMesh mesh, ShaderState shaderState) {
		this.instancer = instancer;
		this.mesh = mesh;
		this.shaderState = shaderState;

		mesh.acquire();

		vao = GlVertexArray.create();
	}

	public boolean deleted() {
		return deleted;
	}

	public void render() {
		if (mesh.invalid()) {
			return;
		}

		instancer.bindIfNeeded(vao, InternalVertex.ATTRIBUTE_COUNT);
		mesh.setup(vao);

		vao.bindForDraw();

		mesh.draw(instancer.instanceCount());
	}

	public void renderOne(InstanceHandleImpl impl) {
		if (mesh.invalid()) {
			return;
		}

		int instanceCount = instancer.instanceCount();
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
		if (deleted) {
			return;
		}

		vao.delete();

		if (vaoScratch != null) {
			vaoScratch.delete();
			vaoScratch = null;
		}

		mesh.drop();

		deleted = true;
	}
}
