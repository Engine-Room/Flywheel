package com.jozufozu.flywheel.backend.model;

import com.jozufozu.flywheel.core.model.Model;

public interface ModelAllocator {
	/**
	 * Allocate a model.
	 *
	 * @param model The model to allocate.
	 * @return A handle to the allocated model.
	 */
	BufferedModel alloc(Model model, Callback allocationCallback);

	@FunctionalInterface
	interface Callback {
		void onAlloc(BufferedModel arenaModel);
	}
}
