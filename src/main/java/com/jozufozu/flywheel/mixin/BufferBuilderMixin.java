package com.jozufozu.flywheel.mixin;

import java.nio.ByteBuffer;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.lwjgl.system.MemoryUtil;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import com.jozufozu.flywheel.backend.model.BufferBuilderExtension;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.blaze3d.vertex.VertexFormatElement;

@Mixin(BufferBuilder.class)
public abstract class BufferBuilderMixin implements BufferBuilderExtension {
	@Shadow
	private ByteBuffer buffer;

	@Shadow
	private int nextElementByte;

	@Shadow
	private int vertices;

	@Shadow
	@Nullable
	private VertexFormatElement currentElement;

	@Shadow
	private int elementIndex;

	@Shadow
	private VertexFormat format;

	@Shadow
	private VertexFormat.Mode mode;

	@Shadow
	private boolean building;

	@Shadow
	private void ensureCapacity(int increaseAmount) {
	}

	@Override
	public int flywheel$getVertices() {
		return vertices;
	}

	@Override
	public void flywheel$freeBuffer() {
		if (this.buffer != null) {
			MemoryUtil.memFree(this.buffer);
			this.buffer = null;
		}
	}

	@Override
	public void flywheel$injectForRender(@Nonnull ByteBuffer buffer, @Nonnull VertexFormat format, int vertexCount) {
		this.building = true;
		this.mode = VertexFormat.Mode.QUADS;

		this.buffer = buffer;
		this.format = format;
		this.vertices = vertexCount;

		this.currentElement = this.format.getElements().get(0);
		this.elementIndex = 0;
	}

	@Override
	public void flywheel$appendBufferUnsafe(ByteBuffer buffer) {
		if (!building) {
			throw new IllegalStateException("BufferBuilder not started");
		}
		if (elementIndex != 0) {
			throw new IllegalStateException("Cannot append buffer while building vertex");
		}

		int numBytes = buffer.remaining();
		if (numBytes % format.getVertexSize() != 0) {
			throw new IllegalArgumentException("Cannot append buffer with non-whole number of vertices");
		}
		int numVertices = numBytes / format.getVertexSize();

		ensureCapacity(numBytes + format.getVertexSize());
		int originalPosition = this.buffer.position();
		this.buffer.position(nextElementByte);
		MemoryUtil.memCopy(buffer, this.buffer);
		this.buffer.position(originalPosition);

		nextElementByte += numBytes;
		vertices += numVertices;
	}
}
