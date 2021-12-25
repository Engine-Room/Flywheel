package com.jozufozu.flywheel.backend.model;

import com.jozufozu.flywheel.core.model.Model;

public class ImmediateAllocator implements ModelAllocator {

	public static final ImmediateAllocator INSTANCE = new ImmediateAllocator();

	@Override
	public BufferedModel alloc(Model model, Callback allocationCallback) {
		IndexedModel out = new IndexedModel(model);
		allocationCallback.onAlloc(out);
		return out;
	}
}
