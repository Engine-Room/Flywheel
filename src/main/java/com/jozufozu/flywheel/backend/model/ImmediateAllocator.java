package com.jozufozu.flywheel.backend.model;

import com.jozufozu.flywheel.core.model.IModel;

public class ImmediateAllocator implements ModelAllocator {

	public static final ImmediateAllocator INSTANCE = new ImmediateAllocator();

	@Override
	public IBufferedModel alloc(IModel model, Callback allocationCallback) {
		IndexedModel out = new IndexedModel(model);
		allocationCallback.onAlloc(out);
		return out;
	}
}
