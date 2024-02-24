package com.jozufozu.flywheel.lib.model;

import java.util.List;

import org.joml.Vector4fc;

import com.google.common.collect.ImmutableList;
import com.jozufozu.flywheel.api.material.Material;
import com.jozufozu.flywheel.api.model.Mesh;
import com.jozufozu.flywheel.api.model.Model;

public class SingleMeshModel implements Model {
	private final Mesh mesh;
	private final Material material;

	public SingleMeshModel(Mesh mesh, Material material) {
		this.mesh = mesh;
		this.material = material;
	}

	@Override
	public List<ConfiguredMesh> meshes() {
		return ImmutableList.of(new ConfiguredMesh(material, mesh));
	}

	@Override
	public Vector4fc boundingSphere() {
		return mesh.boundingSphere();
	}

	@Override
	public void delete() {
		mesh.delete();
	}
}
