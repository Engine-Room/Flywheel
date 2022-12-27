package com.jozufozu.flywheel.backend.model;

import com.jozufozu.flywheel.backend.gl.GlVertexArray;
import com.jozufozu.flywheel.core.model.Model;

public class ArrayModelRenderer {

	private final Model model;
	protected GlVertexArray vao;
	protected BufferedModel vbo;
	protected boolean initialized;

	public ArrayModelRenderer(Model model) {
		this.model = model;
	}

	public Model getModel() {
		return model;
	}

	/**
	 * Renders this model, checking first if there is anything to render.
	 */
	public void draw() {
		if (!initialized) init();
		if (!isValid()) return;

		vao.bind();

		vbo.drawCall();
	}

	protected void init() {
		initialized = true;

		if (model.empty()) return;

		this.vbo = new IndexedModel(model);

		vao = new GlVertexArray();

		vao.bind();

		// bind the model's vbo to our vao
		this.vbo.setupState(vao);

		GlVertexArray.unbind();
	}

	public void delete() {
		if (vbo != null)
			vbo.delete();
	}

	protected boolean isValid() {
		return vbo != null && vbo.valid();
	}
}
