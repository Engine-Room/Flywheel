package com.jozufozu.flywheel.core.vertex;

import java.nio.ByteBuffer;

import org.lwjgl.system.MemoryUtil;

import com.jozufozu.flywheel.api.vertex.VertexList;
import com.jozufozu.flywheel.api.vertex.VertexType;
import com.jozufozu.flywheel.api.vertex.VertexWriter;

public abstract class VertexWriterUnsafe<V extends VertexType> implements VertexWriter {

	public final V type;
	protected final ByteBuffer buffer;
	protected long ptr;

	protected VertexWriterUnsafe(V type, ByteBuffer buffer) {
		this.type = type;
		this.buffer = buffer;
		this.ptr = MemoryUtil.memAddress(buffer);
	}

	@Override
	public void seek(long offset) {
		buffer.position((int) offset);
		ptr = MemoryUtil.memAddress(buffer);
	}

	@Override
	public VertexList intoReader(int vertices) {
		return type.createReader(buffer, vertices);
	}
}
