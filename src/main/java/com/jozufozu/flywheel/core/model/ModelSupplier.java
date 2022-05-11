package com.jozufozu.flywheel.core.model;

import java.util.Map;

import net.minecraft.client.renderer.RenderType;

public interface ModelSupplier {

	Map<RenderType, Mesh> get();

	int getVertexCount();
}
