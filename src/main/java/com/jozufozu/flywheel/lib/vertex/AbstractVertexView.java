package com.jozufozu.flywheel.lib.vertex;

import org.lwjgl.system.MemoryUtil;

import com.jozufozu.flywheel.api.vertex.MutableVertexList;
import com.jozufozu.flywheel.api.vertex.VertexView;

public abstract class AbstractVertexView implements VertexView {
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

	@Override
	public void write(MutableVertexList dst, int srcIndex, int dstIndex) {
		if (dst.getClass() == getClass()) {
			long stride = stride();
			long dstPtr = ((AbstractVertexView) dst).ptr;
			MemoryUtil.memCopy(ptr + srcIndex * stride, dstPtr + dstIndex * stride, stride);
		} else {
			VertexView.super.write(dst, srcIndex, dstIndex);
		}
	}

	@Override
	public void write(MutableVertexList dst, int srcStartIndex, int dstStartIndex, int vertexCount) {
		if (dst.getClass() == getClass()) {
			long stride = stride();
			long dstPtr = ((AbstractVertexView) dst).ptr;
			MemoryUtil.memCopy(ptr + srcStartIndex * stride, dstPtr + dstStartIndex * stride, vertexCount * stride);
		} else {
			VertexView.super.write(dst, srcStartIndex, dstStartIndex, vertexCount);
		}
	}

	@Override
	public void writeAll(MutableVertexList dst) {
		if (dst.getClass() == getClass()) {
			long stride = stride();
			long dstPtr = ((AbstractVertexView) dst).ptr;
			MemoryUtil.memCopy(ptr, dstPtr, Math.min(vertexCount, dst.vertexCount()) * stride);
		} else {
			VertexView.super.writeAll(dst);
		}
	}
}
