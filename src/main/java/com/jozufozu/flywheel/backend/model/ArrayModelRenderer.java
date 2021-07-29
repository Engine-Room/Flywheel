package com.jozufozu.flywheel.backend.model;

import java.util.function.Supplier;

import com.jozufozu.flywheel.backend.gl.GlVertexArray;
import com.jozufozu.flywheel.core.model.IModel;
import com.jozufozu.flywheel.util.AttribUtil;

public class ArrayModelRenderer extends ModelRenderer {

	protected GlVertexArray vao;

	public ArrayModelRenderer(Supplier<IModel> model) {
		super(model);
	}

	@Override
	public void draw() {
		if (!isInitialized()) init();
		if (!isValid()) return;

		vao.bind();

		model.drawCall();

		vao.unbind();
	}

	private boolean isValid() {
		return model != null && model.valid();
	}

	@Override
	protected void init() {
		initialized = true;
		IModel model = modelSupplier.get();

		if (model.vertexCount() <= 0) return;

		this.model = new IndexedModel(model);

		vao = new GlVertexArray();

		vao.bind();

		// bind the model's vbo to our vao
		this.model.setupState();

		AttribUtil.enableArrays(this.model.getAttributeCount());

		vao.unbind();

		this.model.clearState();
	}
}
