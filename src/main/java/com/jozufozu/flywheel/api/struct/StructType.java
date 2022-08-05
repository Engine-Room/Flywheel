package com.jozufozu.flywheel.api.struct;

import java.nio.ByteBuffer;

import com.jozufozu.flywheel.api.instancer.InstancedPart;
import com.jozufozu.flywheel.api.vertex.MutableVertexList;
import com.jozufozu.flywheel.core.layout.BufferLayout;
import com.jozufozu.flywheel.core.source.FileResolution;

import net.minecraft.client.multiplayer.ClientLevel;

/**
 * A StructType contains metadata for a specific instance struct that Flywheel can interface with.
 * @param <S> The java representation of the instance struct.
 */
public interface StructType<S extends InstancedPart> {

	/**
	 * @return A new, zeroed instance of S.
	 */
	S create();

	/**
	 * @return The layout of S when buffered.
	 */
	BufferLayout getLayout();

	/**
	 * Create a {@link StructWriter} that will consume instances of S and write them to the given buffer.
	 *
	 * @param backing The buffer that the StructWriter will write to.
	 */
	StructWriter<S> getWriter(ByteBuffer backing);

	FileResolution getInstanceShader();

	VertexTransformer<? extends S> getVertexTransformer();

	StorageBufferWriter<S> getStorageBufferWriter();

	public interface VertexTransformer<S extends InstancedPart> {
		void transform(MutableVertexList vertexList, S struct, ClientLevel level);
	}

}
