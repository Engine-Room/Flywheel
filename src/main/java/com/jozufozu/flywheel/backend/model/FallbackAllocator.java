package com.jozufozu.flywheel.backend.model;

import com.jozufozu.flywheel.backend.gl.GlVertexArray;
import com.jozufozu.flywheel.core.model.Model;

public enum FallbackAllocator implements ModelAllocator {
	INSTANCE;

	@Override
	public BufferedModel alloc(Model model, GlVertexArray vao) {
		IndexedModel out = new IndexedModel(model);
		vao.bind();
		out.setupState(vao);
		return out;
	}
}
