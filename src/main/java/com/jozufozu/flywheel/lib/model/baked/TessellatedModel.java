package com.jozufozu.flywheel.lib.model.baked;

import java.util.Map;

import com.google.common.collect.ImmutableMap;
import com.jozufozu.flywheel.api.material.Material;
import com.jozufozu.flywheel.api.model.Mesh;
import com.jozufozu.flywheel.api.model.Model;

public class TessellatedModel implements Model {
	private final ImmutableMap<Material, Mesh> meshes;
	private final boolean shadeSeparated;

	public TessellatedModel(ImmutableMap<Material, Mesh> meshes, boolean shadeSeparated) {
		this.meshes = meshes;
		this.shadeSeparated = shadeSeparated;
	}

	@Override
	public Map<Material, Mesh> getMeshes() {
		return meshes;
	}

	@Override
	public void delete() {
		meshes.values()
				.forEach(Mesh::delete);
	}

	public boolean isShadeSeparated() {
		return shadeSeparated;
	}
}
