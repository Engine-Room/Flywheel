package com.jozufozu.flywheel.lib.model.baked;

import com.google.common.collect.ImmutableMap;
import com.jozufozu.flywheel.api.material.Material;
import com.jozufozu.flywheel.api.model.Mesh;
import com.jozufozu.flywheel.lib.model.SimpleModel;

public class TessellatedModel extends SimpleModel {
	private final boolean shadeSeparated;

	public TessellatedModel(ImmutableMap<Material, Mesh> meshes, boolean shadeSeparated) {
		super(meshes);
		this.shadeSeparated = shadeSeparated;
	}

	public boolean isShadeSeparated() {
		return shadeSeparated;
	}
}
