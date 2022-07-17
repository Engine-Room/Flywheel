package com.jozufozu.flywheel.core.vertex;

import java.nio.Buffer;
import java.nio.ByteBuffer;

import org.lwjgl.system.MemoryUtil;

import com.jozufozu.flywheel.api.vertex.VertexList;
import com.mojang.blaze3d.platform.MemoryTracker;
import com.mojang.blaze3d.vertex.BufferBuilder;

public abstract class AbstractVertexList implements VertexList, AutoCloseable {

	protected final ByteBuffer contents;
	protected final long base;
	protected final int vertexCount;

	protected AbstractVertexList(ByteBuffer copyFrom, int vertexCount) {
		this.contents = MemoryTracker.create(copyFrom.capacity());
		this.vertexCount = vertexCount;
		this.base = MemoryUtil.memAddress(this.contents);
		init(copyFrom);
	}

	private void init(ByteBuffer copyFrom) {
		this.contents.order(copyFrom.order());
		this.contents.put(copyFrom);
		((Buffer) this.contents).flip();
	}

	@Override
	public void close() {
		MemoryUtil.memFree(contents);
	}

	@Override
	public int getVertexCount() {
		return vertexCount;
	}
}
