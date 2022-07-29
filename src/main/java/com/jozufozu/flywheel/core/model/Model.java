package com.jozufozu.flywheel.core.model;

import java.util.Map;

import com.jozufozu.flywheel.api.material.Material;

public interface Model {
	Map<Material, Mesh> getMeshes();

	default int getVertexCount() {
		int size = 0;
		for (Mesh mesh : getMeshes().values()) {
			size += mesh.getVertexCount();
		}
		return size;
	}
}
