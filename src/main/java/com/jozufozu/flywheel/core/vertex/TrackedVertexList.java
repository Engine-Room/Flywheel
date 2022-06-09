package com.jozufozu.flywheel.core.vertex;

import java.lang.ref.Cleaner;
import java.nio.Buffer;
import java.nio.ByteBuffer;

import org.lwjgl.system.MemoryUtil;

import com.jozufozu.flywheel.api.vertex.VertexList;
import com.jozufozu.flywheel.backend.FlywheelMemory;
import com.mojang.blaze3d.platform.MemoryTracker;

public abstract class TrackedVertexList implements VertexList, AutoCloseable {

	protected final ByteBuffer contents;
	protected final long base;
	protected final int vertexCount;
	private final Cleaner.Cleanable cleanable;

	protected TrackedVertexList(ByteBuffer copyFrom, int vertexCount) {
		this.contents = MemoryTracker.create(copyFrom.capacity());
		this.contents.order(copyFrom.order());
		this.contents.put(copyFrom);
		((Buffer) this.contents).flip();

		this.cleanable = FlywheelMemory.track(this, this.contents);

		this.base = MemoryUtil.memAddress(this.contents);
		this.vertexCount = vertexCount;
	}

	@Override
	public void close() {
		cleanable.clean();
	}

	@Override
	public int getVertexCount() {
		return vertexCount;
	}
}
