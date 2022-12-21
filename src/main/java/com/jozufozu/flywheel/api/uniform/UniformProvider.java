package com.jozufozu.flywheel.api.uniform;

import com.jozufozu.flywheel.core.source.FileResolution;

public interface UniformProvider {

	int byteSize();

	FileResolution uniformShader();

	ActiveUniformProvider activate(long ptr);

	interface ActiveUniformProvider {
		void delete();

		/**
		 * Poll the provider for changes.
		 *
		 * @return {@code true} if the provider updated its backing store.
		 */
		boolean poll();
	}
}
