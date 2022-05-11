package com.jozufozu.flywheel.backend.model;

import com.jozufozu.flywheel.backend.gl.GlVertexArray;
import com.jozufozu.flywheel.core.model.BlockMesh;

public class ArrayModelRenderer {

	protected final GlVertexArray vao;
	protected final MeshPool.BufferedMesh mesh;

	public ArrayModelRenderer(BlockMesh mesh, MeshPool meshPool) {
		this.vao = new GlVertexArray();
		this.mesh = meshPool.alloc(mesh, this.vao);
	}

	/**
	 * Renders this model, checking first if there is anything to render.
	 */
	public void draw() {
		if (mesh.isDeleted()) return;

		vao.bind();

		mesh.drawCall();
	}

	public void delete() {
		mesh.delete();
	}

}
