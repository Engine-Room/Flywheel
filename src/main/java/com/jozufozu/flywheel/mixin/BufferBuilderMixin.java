package com.jozufozu.flywheel.mixin;

import java.nio.ByteBuffer;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.lwjgl.system.MemoryUtil;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import com.jozufozu.flywheel.backend.model.BufferBuilderHack;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.blaze3d.vertex.VertexFormatElement;

@Mixin(BufferBuilder.class)
public abstract class BufferBuilderMixin implements BufferBuilderHack {
	@Shadow
	private ByteBuffer buffer;

	@Shadow
	private boolean building;

	@Shadow
	public abstract void begin(VertexFormat.Mode p_166780_, VertexFormat p_166781_);

	@Shadow
	private VertexFormat.Mode mode;

	@Shadow
	private VertexFormat format;

	@Shadow
	@Nullable
	private VertexFormatElement currentElement;

	@Shadow
	private int elementIndex;

	@Shadow
	private int vertices;

	@Override
	public void flywheel$freeBuffer() {
		if (this.buffer != null) {
			MemoryUtil.memFree(this.buffer);
			this.buffer = null;
		}
	}

	@Override
	public void flywheel$hackBegin(@Nonnull ByteBuffer buffer, @Nonnull VertexFormat format, int vertexCount) {
		this.building = true;
		this.mode = VertexFormat.Mode.QUADS;

		this.buffer = buffer;
		this.format = format;
		this.vertices = vertexCount;

		this.currentElement = this.format.getElements().get(0);
		this.elementIndex = 0;
	}
}
