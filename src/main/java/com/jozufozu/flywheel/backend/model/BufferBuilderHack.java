package com.jozufozu.flywheel.backend.model;

import java.nio.ByteBuffer;

import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.VertexFormat;

/**
 * Duck interface used on {@link BufferBuilder} to provide lower level access to the backing memory.
 */
public interface BufferBuilderHack {

	void freeBuffer();

	void hackBegin(ByteBuffer buffer, VertexFormat format, int vertexCount);
}
