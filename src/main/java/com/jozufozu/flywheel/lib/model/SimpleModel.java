package com.jozufozu.flywheel.lib.model;

import java.util.Map;

import org.joml.Vector4fc;

import com.google.common.collect.ImmutableMap;
import com.jozufozu.flywheel.api.material.Material;
import com.jozufozu.flywheel.api.model.Mesh;
import com.jozufozu.flywheel.api.model.Model;

public class SimpleModel implements Model {
	private final ImmutableMap<Material, Mesh> meshes;
	private final Vector4fc boundingSphere;
	private final int vertexCount;

	public SimpleModel(ImmutableMap<Material, Mesh> meshes) {
		this.meshes = meshes;
		this.boundingSphere = ModelUtil.computeBoundingSphere(meshes.values());
		this.vertexCount = ModelUtil.computeTotalVertexCount(meshes.values());
	}

	@Override
	public Map<Material, Mesh> meshes() {
		return meshes;
	}

	@Override
	public Vector4fc boundingSphere() {
		return boundingSphere;
	}

	@Override
	public int vertexCount() {
		return vertexCount;
	}

	@Override
	public void delete() {
		meshes.values()
				.forEach(Mesh::delete);
	}
}
