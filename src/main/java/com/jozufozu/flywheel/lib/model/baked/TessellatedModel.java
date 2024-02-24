package com.jozufozu.flywheel.lib.model.baked;

import com.google.common.collect.ImmutableList;
import com.jozufozu.flywheel.lib.model.SimpleModel;

public class TessellatedModel extends SimpleModel {
	private final boolean shadeSeparated;

	public TessellatedModel(ImmutableList<ConfiguredMesh> meshes, boolean shadeSeparated) {
		super(meshes);
		this.shadeSeparated = shadeSeparated;
	}

	public boolean isShadeSeparated() {
		return shadeSeparated;
	}
}
