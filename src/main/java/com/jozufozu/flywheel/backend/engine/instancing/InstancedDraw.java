package com.jozufozu.flywheel.backend.engine.instancing;

import com.jozufozu.flywheel.api.material.Material;
import com.jozufozu.flywheel.backend.engine.GroupKey;
import com.jozufozu.flywheel.backend.engine.MeshPool;
import com.jozufozu.flywheel.backend.gl.TextureBuffer;

public class InstancedDraw {
	public final GroupKey<?> groupKey;
	private final InstancedInstancer<?> instancer;
	private final MeshPool.PooledMesh mesh;
	private final Material material;
	private final int indexOfMeshInModel;

	private boolean deleted;

	public InstancedDraw(InstancedInstancer<?> instancer, MeshPool.PooledMesh mesh, GroupKey<?> groupKey, Material material, int indexOfMeshInModel) {
		this.instancer = instancer;
		this.mesh = mesh;
		this.groupKey = groupKey;
		this.material = material;
		this.indexOfMeshInModel = indexOfMeshInModel;

		mesh.acquire();
	}

	public int indexOfMeshInModel() {
		return indexOfMeshInModel;
	}

	public Material material() {
		return material;
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

	public void renderOne(TextureBuffer buffer) {
		if (mesh.isInvalid()) {
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
