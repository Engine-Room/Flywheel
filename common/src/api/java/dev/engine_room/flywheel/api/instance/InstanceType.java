package com.jozufozu.flywheel.api.instance;

import com.jozufozu.flywheel.api.internal.FlwApiLink;
import com.jozufozu.flywheel.api.layout.Layout;
import com.jozufozu.flywheel.api.registry.Registry;

import net.minecraft.resources.ResourceLocation;

/**
 * An InstanceType contains metadata for a specific instance that Flywheel can interface with.
 *
 * @param <I> The java representation of the instance.
 */
public interface InstanceType<I extends Instance> {
	static Registry<InstanceType<?>> REGISTRY = FlwApiLink.INSTANCE.createRegistry();

	/**
	 * @param handle A handle that allows you to mark the instance as dirty or deleted.
	 * @return A new, zeroed instance of I.
	 */
	I create(InstanceHandle handle);

	Layout layout();

	InstanceWriter<I> writer();

	ResourceLocation vertexShader();

	ResourceLocation cullShader();
}
