package com.jozufozu.flywheel.lib.model;

import java.util.Map;

import com.google.common.collect.ImmutableMap;
import com.jozufozu.flywheel.api.material.Material;
import com.jozufozu.flywheel.api.model.Mesh;
import com.jozufozu.flywheel.api.model.Model;

public class SimpleModel implements Model {
	private final Mesh mesh;
	private final Map<Material, Mesh> meshMap;

	public SimpleModel(Mesh mesh, Material material) {
		this.mesh = mesh;
		meshMap = ImmutableMap.of(material, mesh);
	}

	@Override
	public Map<Material, Mesh> getMeshes() {
		return meshMap;
	}

	@Override
	public void delete() {
		mesh.delete();
	}
}
