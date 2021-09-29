package com.jozufozu.flywheel.backend.model;

import java.util.function.Supplier;

import com.jozufozu.flywheel.core.model.Model;

public class ModelRenderer {

	protected Supplier<Model> modelSupplier;
	protected IBufferedModel model;

	protected boolean initialized;

	public ModelRenderer(Supplier<Model> modelSupplier) {
		this.modelSupplier = modelSupplier;
	}

	/**
	 * Renders this model, checking first if there is anything to render.
	 */
	public void draw() {
		if (!initialized) init();
		if (!isValid()) return;

		model.setupState();
		model.drawCall();
		model.clearState();
	}

	public void delete() {
		if (model != null)
			model.delete();
	}

	protected void init() {
		initialized = true;
		Model model = modelSupplier.get();

		if (model.empty()) return;

		this.model = new IndexedModel(model);
	}

	protected boolean isValid() {
		return model != null && model.valid();
	}
}
