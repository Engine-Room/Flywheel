package com.jozufozu.flywheel.backend.model;

import java.util.function.Supplier;

import com.jozufozu.flywheel.backend.gl.GlVertexArray;
import com.jozufozu.flywheel.core.model.Model;
import com.jozufozu.flywheel.util.AttribUtil;

public class ArrayModelRenderer extends ModelRenderer {

	protected GlVertexArray vao;

	public ArrayModelRenderer(Supplier<Model> model) {
		super(model);
	}

	@Override
	public void draw() {
		if (!initialized) init();
		if (!isValid()) return;

		vao.bind();

		model.drawCall();
	}

	@Override
	protected void init() {
		initialized = true;
		Model model = modelSupplier.get();

		if (model.empty()) return;

		this.model = new IndexedModel(model);

		vao = new GlVertexArray();

		vao.bind();
		vao.enableArrays(this.model.getAttributeCount());

		// bind the model's vbo to our vao
		this.model.setupState();

		GlVertexArray.unbind();

		this.model.clearState();
	}
}
