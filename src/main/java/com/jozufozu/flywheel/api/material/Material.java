package com.jozufozu.flywheel.api.material;

import com.jozufozu.flywheel.api.vertex.MutableVertexList;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;

public interface Material {
	ResourceLocation vertexShader();

	ResourceLocation fragmentShader();

	void setup();

	void clear();

	RenderType getBatchingRenderType();

	VertexTransformer getVertexTransformer();

	interface VertexTransformer {
		void transform(MutableVertexList vertexList, ClientLevel level);
	}
}
