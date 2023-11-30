package com.jozufozu.flywheel.api.material;

import net.minecraft.client.renderer.RenderType;

public interface Material {
	void setup();

	void clear();

	MaterialShaders shaders();

	RenderType getFallbackRenderType();

	MaterialVertexTransformer getVertexTransformer();
}
