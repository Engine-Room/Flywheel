package com.jozufozu.flywheel.api.material;

import com.jozufozu.flywheel.api.registry.Registry;
import com.jozufozu.flywheel.api.vertex.MutableVertexList;
import com.jozufozu.flywheel.impl.RegistryImpl;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;

public interface Material {
	static Registry<Material> REGISTRY = RegistryImpl.create();

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
