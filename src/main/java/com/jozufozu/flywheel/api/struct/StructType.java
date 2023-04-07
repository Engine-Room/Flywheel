package com.jozufozu.flywheel.api.struct;

import com.jozufozu.flywheel.api.layout.BufferLayout;
import com.jozufozu.flywheel.api.registry.Registry;
import com.jozufozu.flywheel.impl.RegistryImpl;

import net.minecraft.resources.ResourceLocation;

/**
 * A StructType contains metadata for a specific instance struct that Flywheel can interface with.
 *
 * @param <P> The java representation of the instance struct.
 */
public interface StructType<P extends InstancePart> {
	static Registry<StructType<?>> REGISTRY = RegistryImpl.create();

	/**
	 * @param handle A handle that allows you to mark the instance as dirty or deleted.
	 * @return A new, zeroed instance of S.
	 */
	P create(Handle handle);

	/**
	 * @return The layout of S when buffered.
	 */
	BufferLayout getLayout();

	StructWriter<P> getWriter();

	ResourceLocation instanceShader();

	StructVertexTransformer<P> getVertexTransformer();
}
