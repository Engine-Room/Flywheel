package com.jozufozu.flywheel.backend.model;

import java.util.function.Supplier;

import com.jozufozu.flywheel.core.model.IModel;

public class ModelRenderer {

	protected Supplier<IModel> modelSupplier;
	protected IBufferedModel model;

	protected boolean initialized;

	public ModelRenderer(Supplier<IModel> modelSupplier) {
		this.modelSupplier = modelSupplier;
	}

	/**
	 * Renders this model, checking first if there is anything to render.
	 */
	public void draw() {

		if (!isInitialized()) init();
		if (!model.valid()) return;

		model.setupState();
		model.drawCall();
		model.clearState();
	}

	protected void init() {
		initialized = true;
		IModel model = modelSupplier.get();

		if (model.vertexCount() <= 0) return;

		this.model = new IndexedModel(model);
	}

	public boolean isInitialized() {
		return initialized;
	}

	public void delete() {
		if (model != null)
			model.delete();
	}
}
