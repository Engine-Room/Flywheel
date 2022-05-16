package com.jozufozu.flywheel.core.model;

import java.util.Map;

import com.jozufozu.flywheel.api.material.Material;

public interface ModelSupplier {
	Map<Material, Mesh> get();

	int getVertexCount();
}
