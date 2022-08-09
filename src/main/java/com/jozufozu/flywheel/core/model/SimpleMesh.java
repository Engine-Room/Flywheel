package com.jozufozu.flywheel.core.model;

import java.nio.ByteBuffer;

import org.lwjgl.system.MemoryUtil;

import com.jozufozu.flywheel.api.vertex.MutableVertexList;
import com.jozufozu.flywheel.api.vertex.ReusableVertexList;
import com.jozufozu.flywheel.api.vertex.VertexType;

public class SimpleMesh implements Mesh {
	private final VertexType vertexType;
	private final int vertexCount;
	private final ByteBuffer contents;
	private final ReusableVertexList vertexList;
	private final String name;

	public SimpleMesh(VertexType vertexType, ByteBuffer contents, String name) {
		this.vertexType = vertexType;
		this.contents = contents;
		this.name = name;

		contents.clear();
		int bytes = contents.remaining();
		int stride = vertexType.getStride();
		if (bytes % stride != 0) {
			throw new IllegalArgumentException("Buffer contains non-whole amount of vertices!");
		}
		vertexCount = bytes / stride;

		vertexList = getVertexType().createVertexList();
		vertexList.ptr(MemoryUtil.memAddress(contents));
		vertexList.setVertexCount(vertexCount);
	}

	@Override
	public VertexType getVertexType() {
		return vertexType;
	}

	@Override
	public int getVertexCount() {
		return vertexCount;
	}

	@Override
	public void writeInto(ByteBuffer buffer, long byteIndex) {
		buffer.position((int) byteIndex);
		MemoryUtil.memCopy(contents, buffer);
	}

	@Override
	public void writeInto(MutableVertexList dst) {
		vertexList.writeAll(dst);
	}

	@Override
	public void close() {
		MemoryUtil.memFree(contents);
	}

	@Override
	public String name() {
		return name;
	}

	@Override
	public String toString() {
		return "SimpleMesh{" + "name='" + name + "',vertexType='" + vertexType + "}";
	}
}
