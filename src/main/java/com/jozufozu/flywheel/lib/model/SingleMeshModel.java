package com.jozufozu.flywheel.lib.model;

import java.util.Map;

import org.joml.Vector4fc;

import com.google.common.collect.ImmutableMap;
import com.jozufozu.flywheel.api.material.Material;
import com.jozufozu.flywheel.api.model.Mesh;
import com.jozufozu.flywheel.api.model.Model;

public class SingleMeshModel implements Model {
	private final Mesh mesh;
	private final Map<Material, Mesh> meshMap;

	public SingleMeshModel(Mesh mesh, Material material) {
		this.mesh = mesh;
		meshMap = ImmutableMap.of(material, mesh);
	}

	@Override
	public Map<Material, Mesh> meshes() {
		return meshMap;
	}

	@Override
	public Vector4fc boundingSphere() {
		return mesh.boundingSphere();
	}

	@Override
	public int vertexCount() {
		return mesh.vertexCount();
	}

	@Override
	public void delete() {
		mesh.delete();
	}
}
