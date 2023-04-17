package com.jozufozu.flywheel.api.instance;

import com.jozufozu.flywheel.api.layout.BufferLayout;
import com.jozufozu.flywheel.api.registry.Registry;
import com.jozufozu.flywheel.impl.RegistryImpl;

import net.minecraft.resources.ResourceLocation;

/**
 * An InstanceType contains metadata for a specific instance that Flywheel can interface with.
 *
 * @param <I> The java representation of the instance.
 */
public interface InstanceType<I extends Instance> {
	static Registry<InstanceType<?>> REGISTRY = RegistryImpl.create();

	/**
	 * @param handle A handle that allows you to mark the instance as dirty or deleted.
	 * @return A new, zeroed instance of I.
	 */
	I create(InstanceHandle handle);

	/**
	 * @return The layout of I when buffered.
	 */
	BufferLayout getLayout();

	InstanceWriter<I> getWriter();

	ResourceLocation instanceShader();

	InstanceVertexTransformer<I> getVertexTransformer();

	InstanceBoundingSphereTransformer<I> getBoundingSphereTransformer();
}
