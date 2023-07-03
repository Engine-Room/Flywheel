package com.jozufozu.flywheel.core.model;

import java.nio.ByteBuffer;

import org.lwjgl.system.MemoryUtil;

import com.jozufozu.flywheel.util.FlwUtil;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.BufferBuilder.DrawState;

public interface ShadeSeparatedBufferedData {
	ByteBuffer vertexBuffer();

	ByteBuffer indexBuffer();

	BufferBuilder.DrawState drawState();

	int unshadedStartVertex();

	void release();

	static final class NativeImpl implements ShadeSeparatedBufferedData {
		private final ByteBuffer vertexBuffer;
		private final ByteBuffer indexBuffer;
		private final BufferBuilder.DrawState drawState;
		private final int unshadedStartVertex;

		public NativeImpl(ByteBuffer vertexBuffer, ByteBuffer indexBuffer, BufferBuilder.DrawState drawState, int unshadedStartVertex) {
			this.vertexBuffer = FlwUtil.copyBuffer(vertexBuffer);
			this.indexBuffer = FlwUtil.copyBuffer(indexBuffer);
			this.drawState = drawState;
			this.unshadedStartVertex = unshadedStartVertex;
		}

		@Override
		public ByteBuffer vertexBuffer() {
			return vertexBuffer;
		}

		@Override
		public ByteBuffer indexBuffer() {
			return indexBuffer;
		}

		@Override
		public DrawState drawState() {
			return drawState;
		}

		@Override
		public int unshadedStartVertex() {
			return unshadedStartVertex;
		}

		@Override
		public void release() {
			MemoryUtil.memFree(vertexBuffer);
			MemoryUtil.memFree(indexBuffer);
		}
	}
}
