package com.jozufozu.flywheel.backend.model;

import com.jozufozu.flywheel.backend.gl.GlVertexArray;
import com.jozufozu.flywheel.core.model.Mesh;

public enum FallbackAllocator implements MeshAllocator {
	INSTANCE;

	@Override
	public BufferedModel alloc(Mesh mesh, GlVertexArray vao) {
		IndexedModel out = new IndexedModel(mesh);
		out.setupState(vao);
		return out;
	}
}
