package com.jozufozu.flywheel.api.uniform;

import net.minecraft.resources.ResourceLocation;

public interface ShaderUniforms {

	Provider activate(long ptr);

	ResourceLocation uniformShader();

	int byteSize();

	interface Provider {
		/**
		 * Delete this provider.<p>
		 * <p>
		 * Do not free the ptr passed to {@link #activate(long)}.<br>
		 * Clean up other resources, and unsubscribe from events.
		 */
		void delete();

		/**
		 * Poll the provider for changes.
		 *
		 * @return {@code true} if the provider updated its backing store.
		 */
		boolean poll();
	}
}
