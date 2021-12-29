package com.jozufozu.flywheel.backend;

import com.jozufozu.flywheel.api.shader.FlexibleShader;
import com.jozufozu.flywheel.api.vertex.VertexType;
import com.jozufozu.flywheel.backend.gl.shader.GlProgram;

import net.minecraft.resources.ResourceLocation;

public interface ShaderContext<P extends GlProgram> {

	default P getProgram(ResourceLocation loc, VertexType inputType) {
		return this.getProgramSupplier(loc)
				.get(inputType);
	}

	FlexibleShader<P> getProgramSupplier(ResourceLocation loc);

	/**
	 * Load all programs associated with this context. This might be just one, if the context is very specialized.
	 */
	void load();

	void delete();
}
