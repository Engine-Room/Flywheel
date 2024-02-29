package com.jozufozu.flywheel.backend.engine.instancing;

import com.jozufozu.flywheel.backend.engine.InstanceHandleImpl;
import com.jozufozu.flywheel.backend.engine.MeshPool;
import com.jozufozu.flywheel.backend.gl.TextureBuffer;

public class DrawCall {
	public final ShaderState shaderState;
	private final InstancedInstancer<?> instancer;
	private final MeshPool.PooledMesh mesh;

	private boolean deleted;

	public DrawCall(InstancedInstancer<?> instancer, MeshPool.PooledMesh mesh, ShaderState shaderState) {
		this.instancer = instancer;
		this.mesh = mesh;
		this.shaderState = shaderState;

		mesh.acquire();
	}

	public boolean deleted() {
		return deleted;
	}

	public void render(TextureBuffer buffer) {
		if (mesh.isInvalid()) {
			return;
		}

		instancer.bind(buffer);

		mesh.draw(instancer.instanceCount());
	}

	public void renderOne(TextureBuffer buffer, InstanceHandleImpl impl) {
		if (mesh.isInvalid()) {
			return;
		}

		int instanceCount = instancer.instanceCount();
		if (instanceCount <= 0 || impl.index >= instanceCount) {
			return;
		}

		instancer.bind(buffer);

		mesh.draw(1);
	}

	public void delete() {
		if (deleted) {
			return;
		}

		mesh.release();

		deleted = true;
	}
}
