package com.jozufozu.flywheel.backend.instancing.instancing;

import com.jozufozu.flywheel.api.material.Material;
import com.jozufozu.flywheel.api.vertex.VertexType;

public interface Renderable {

	void render();

	boolean shouldRemove();

	Material getMaterial();

	VertexType getVertexType();
}
