package com.jozufozu.flywheel.api.material;

import com.jozufozu.flywheel.api.vertex.MutableVertexList;
import com.jozufozu.flywheel.core.source.FileResolution;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.RenderType;

public interface Material {
	FileResolution getVertexShader();

	FileResolution getFragmentShader();

	void setup();

	void clear();

	RenderType getBatchingRenderType();

	VertexTransformer getVertexTransformer();

	interface VertexTransformer {
		void transform(MutableVertexList vertexList, ClientLevel level);
	}
}
