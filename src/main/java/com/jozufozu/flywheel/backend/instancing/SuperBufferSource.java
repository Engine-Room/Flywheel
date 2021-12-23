package com.jozufozu.flywheel.backend.instancing;

import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;

import com.jozufozu.flywheel.backend.model.BufferBuilderHack;
import com.jozufozu.flywheel.backend.model.DirectVertexConsumer;
import com.mojang.blaze3d.platform.MemoryTracker;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.VertexFormat;

import net.minecraft.client.renderer.RenderType;

public class SuperBufferSource {

	protected final Map<RenderType, DrawBuffer> buffers = new HashMap<>();
	private final BufferBuilder scratch;

	public SuperBufferSource() {
		scratch = new BufferBuilder(8);

		((BufferBuilderHack) scratch).freeBuffer();
	}

	public DirectVertexConsumer getBuffer(RenderType renderType, int vertexCount) {
		return buffers.computeIfAbsent(renderType, DrawBuffer::new)
				.begin(vertexCount);
	}

	public void endBatch() {
		// TODO: when/if this causes trouble with shaders, try to inject our BufferBuilders
		//  into the RenderBuffers from context.

		BufferBuilderHack hack = (BufferBuilderHack) scratch;

		for (Map.Entry<RenderType, DrawBuffer> entry : buffers.entrySet()) {
			DrawBuffer builder = entry.getValue();

			if (builder.expectedVertices > 0) {
				RenderType type = entry.getKey();

				hack.hackBegin(builder.backingBuffer, type.format(), builder.expectedVertices);

				type.end(scratch, 0, 0, 0);

				builder.expectedVertices = 0;
			}
		}
	}

	private static class DrawBuffer {

		private final RenderType type;
		private ByteBuffer backingBuffer;
		private int expectedVertices;

		public DrawBuffer(RenderType type) {
			this.type = type;
		}

		public DirectVertexConsumer begin(int vertexCount) {
			this.expectedVertices = vertexCount;

			VertexFormat format = type.format();

			int byteSize = format.getVertexSize() * vertexCount;

			if (backingBuffer == null) {
				backingBuffer = MemoryTracker.create(byteSize);
			} if (byteSize > backingBuffer.capacity()) {
				backingBuffer = MemoryTracker.resize(backingBuffer, byteSize);
			}

			return new DirectVertexConsumer(backingBuffer, format, vertexCount);
		}

	}
}
