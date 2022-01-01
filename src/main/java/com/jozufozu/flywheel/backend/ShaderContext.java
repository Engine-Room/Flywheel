package com.jozufozu.flywheel.backend;

import com.jozufozu.flywheel.api.vertex.VertexType;
import com.jozufozu.flywheel.backend.gl.shader.GlProgram;

import net.minecraft.resources.ResourceLocation;

public interface ShaderContext<P extends GlProgram> {

	P getProgram(ResourceLocation loc, VertexType vertexType, RenderLayer layer);

	/**
	 * Load all programs associated with this context. This might be just one, if the context is very specialized.
	 */
	void load();

	void delete();
}
