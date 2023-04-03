package com.jozufozu.flywheel.api.struct;

import com.jozufozu.flywheel.api.instancer.InstancedPart;
import com.jozufozu.flywheel.api.layout.BufferLayout;
import com.jozufozu.flywheel.api.registry.Registry;
import com.jozufozu.flywheel.api.vertex.MutableVertexList;
import com.jozufozu.flywheel.impl.RegistryImpl;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.resources.ResourceLocation;

/**
 * A StructType contains metadata for a specific instance struct that Flywheel can interface with.
 * @param <S> The java representation of the instance struct.
 */
public interface StructType<S extends InstancedPart> {
	static Registry<StructType<?>> REGISTRY = RegistryImpl.create();

	/**
	 * @return A new, zeroed instance of S.
	 */
	S create();

	/**
	 * @return The layout of S when buffered.
	 */
	BufferLayout getLayout();

	StructWriter<S> getWriter();

	ResourceLocation instanceShader();

	VertexTransformer<S> getVertexTransformer();

	interface VertexTransformer<S extends InstancedPart> {
		void transform(MutableVertexList vertexList, S struct, ClientLevel level);
	}

}
