package com.jozufozu.flywheel.core.vertex;

import java.nio.ByteBuffer;

import org.lwjgl.system.MemoryUtil;

import com.jozufozu.flywheel.api.vertex.VertexList;
import com.jozufozu.flywheel.util.FlwUtil;

public abstract class AbstractVertexList implements VertexList {

	protected final ByteBuffer contents;
	protected final long base;
	protected final int vertexCount;

	protected AbstractVertexList(ByteBuffer copyFrom, int vertexCount) {
		this.contents = FlwUtil.copyBuffer(copyFrom);
		this.vertexCount = vertexCount;
		this.base = MemoryUtil.memAddress(this.contents);
	}

	@Override
	public int getVertexCount() {
		return vertexCount;
	}

	@Override
	public void delete() {
		MemoryUtil.memFree(contents);
	}
}
