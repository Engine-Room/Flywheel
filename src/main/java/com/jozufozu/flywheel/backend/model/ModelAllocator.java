package com.jozufozu.flywheel.backend.model;

import com.jozufozu.flywheel.core.model.IModel;

public interface ModelAllocator {
	/**
	 * Allocate a model.
	 *
	 * @param model The model to allocate.
	 * @return A handle to the allocated model.
	 */
	IBufferedModel alloc(IModel model, Callback allocationCallback);

	@FunctionalInterface
	interface Callback {
		void onAlloc(IBufferedModel arenaModel);
	}
}
