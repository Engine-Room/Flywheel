package com.jozufozu.flywheel.lib.format;

import com.jozufozu.flywheel.api.vertex.ReusableVertexList;

public abstract class AbstractVertexList implements ReusableVertexList {
	protected long ptr;
	protected int vertexCount;

	@Override
	public long ptr() {
		return ptr;
	}

	@Override
	public void ptr(long ptr) {
		this.ptr = ptr;
	}

	@Override
	public int vertexCount() {
		return vertexCount;
	}

	@Override
	public void vertexCount(int vertexCount) {
		this.vertexCount = vertexCount;
	}
}
