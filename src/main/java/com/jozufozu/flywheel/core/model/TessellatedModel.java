package com.jozufozu.flywheel.core.model;

import java.util.Map;

import com.google.common.collect.ImmutableMap;
import com.jozufozu.flywheel.api.material.Material;

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

	public boolean isShadeSeparated() {
		return shadeSeparated;
	}
}
