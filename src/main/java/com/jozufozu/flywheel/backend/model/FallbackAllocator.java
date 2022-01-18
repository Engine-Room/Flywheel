package com.jozufozu.flywheel.backend.model;

import com.jozufozu.flywheel.core.model.Model;

public enum FallbackAllocator implements ModelAllocator {
	INSTANCE;

	@Override
	public BufferedModel alloc(Model model, Callback allocationCallback) {
		IndexedModel out = new IndexedModel(model);
		allocationCallback.onAlloc(out);
		return out;
	}
}
