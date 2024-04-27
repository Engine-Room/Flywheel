package com.jozufozu.flywheel.lib.model.baked;

import org.jetbrains.annotations.UnknownNullability;

import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;

import net.minecraft.client.renderer.RenderType;

class MeshEmitter {
	private final RenderType renderType;
	private final BufferBuilder bufferBuilder;

	private BakedModelBufferer.@UnknownNullability ResultConsumer resultConsumer;
	private boolean currentShade;

	MeshEmitter(RenderType renderType) {
		this.renderType = renderType;
		this.bufferBuilder = new BufferBuilder(renderType.bufferSize());
	}

	public void prepare(BakedModelBufferer.ResultConsumer resultConsumer) {
		this.resultConsumer = resultConsumer;
	}

	public void end() {
		if (bufferBuilder.building()) {
			emit();
		}
		resultConsumer = null;
	}

	public BufferBuilder getBuffer(boolean shade) {
		prepareForGeometry(shade);
		return bufferBuilder;
	}

	void prepareForGeometry(boolean shade) {
		if (!bufferBuilder.building()) {
			bufferBuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.BLOCK);
		} else if (shade != currentShade) {
			emit();
		}

		currentShade = shade;
	}

	void emit() {
		var renderedBuffer = bufferBuilder.endOrDiscardIfEmpty();

		if (renderedBuffer != null) {
			resultConsumer.accept(renderType, currentShade, renderedBuffer);
			renderedBuffer.release();
		}
	}
}
