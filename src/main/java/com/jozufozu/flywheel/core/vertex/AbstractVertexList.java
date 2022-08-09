package com.jozufozu.flywheel.core.vertex;

import com.jozufozu.flywheel.api.vertex.ReusableVertexList;

public abstract class AbstractVertexList implements ReusableVertexList {
	protected long ptr;
	protected int vertexCount;

	@Override
	public int getVertexCount() {
		return vertexCount;
	}

	@Override
	public void setVertexCount(int vertexCount) {
		this.vertexCount = vertexCount;
	}

	@Override
	public long ptr() {
		return ptr;
	}

	@Override
	public void ptr(long ptr) {
		this.ptr = ptr;
	}
}
