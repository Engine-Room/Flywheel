package com.jozufozu.flywheel.core.vertex;

import java.nio.ByteBuffer;

import org.lwjgl.system.MemoryUtil;

import com.jozufozu.flywheel.api.vertex.VertexList;
import com.jozufozu.flywheel.api.vertex.VertexType;
import com.jozufozu.flywheel.api.vertex.VertexWriter;

public abstract class VertexWriterUnsafe<V extends VertexType> implements VertexWriter {

	public final V type;
	protected final ByteBuffer buffer;
	private int totalVertices;
	private int writeVertex;
	protected long ptr;

	protected VertexWriterUnsafe(V type, ByteBuffer buffer) {
		this.type = type;
		this.buffer = buffer;
		this.ptr = MemoryUtil.memAddress(buffer);
	}

	protected void advance() {
		writeVertex++;
		// account for seeking
		if (writeVertex > totalVertices) totalVertices = writeVertex;
	}

	@Override
	public void seekToVertex(int vertex) {
		buffer.position(type.byteOffset(vertex));
		writeVertex = vertex;
		ptr = MemoryUtil.memAddress(buffer);
	}

	@Override
	public VertexList intoReader() {
		return type.createReader(buffer, totalVertices);
	}
}
