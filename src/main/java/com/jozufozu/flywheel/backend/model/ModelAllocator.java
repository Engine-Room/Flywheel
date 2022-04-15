package com.jozufozu.flywheel.backend.model;

import com.jozufozu.flywheel.backend.gl.GlVertexArray;
import com.jozufozu.flywheel.core.model.Model;

public interface ModelAllocator {
	/**
	 * Allocate a model.
	 *
	 * @param model The model to allocate.
	 * @param vao   The vertex array object to attach the model to.
	 * @return A handle to the allocated model.
	 */
	BufferedModel alloc(Model model, GlVertexArray vao);

	@FunctionalInterface
	interface Callback {
		void onAlloc(BufferedModel arenaModel);
	}
}
