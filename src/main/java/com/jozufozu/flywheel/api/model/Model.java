package com.jozufozu.flywheel.api.model;

import java.util.Map;

import com.jozufozu.flywheel.api.material.Material;

public interface Model {
	Map<Material, Mesh> getMeshes();

	void delete();

	default int getVertexCount() {
		int size = 0;
		for (Mesh mesh : getMeshes().values()) {
			size += mesh.vertexCount();
		}
		return size;
	}
}
