package dev.engine_room.flywheel.lib.model.baked;

import org.jetbrains.annotations.UnknownNullability;

import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.ByteBufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;

import net.minecraft.client.renderer.RenderType;

class MeshEmitter {
	private final RenderType renderType;
	private final ByteBufferBuilder byteBufferBuilder;
	@UnknownNullability
	private BufferBuilder bufferBuilder;

	private BakedModelBufferer.@UnknownNullability ResultConsumer resultConsumer;
	private boolean currentShade;

	MeshEmitter(RenderType renderType) {
		this.renderType = renderType;
		this.byteBufferBuilder = new ByteBufferBuilder(renderType.bufferSize());
	}

	public void prepare(BakedModelBufferer.ResultConsumer resultConsumer) {
		this.resultConsumer = resultConsumer;
	}

	public void end() {
		if (bufferBuilder != null) {
			emit();
		}
		resultConsumer = null;
	}

	public BufferBuilder getBuffer(boolean shade) {
		prepareForGeometry(shade);
		return bufferBuilder;
	}

	private void prepareForGeometry(boolean shade) {
		if (bufferBuilder == null) {
			bufferBuilder = new BufferBuilder(byteBufferBuilder, VertexFormat.Mode.QUADS, DefaultVertexFormat.BLOCK);
		} else if (shade != currentShade) {
			emit();
			bufferBuilder = new BufferBuilder(byteBufferBuilder, VertexFormat.Mode.QUADS, DefaultVertexFormat.BLOCK);
		}

		currentShade = shade;
	}

	private void emit() {
		var data = bufferBuilder.build();
		bufferBuilder = null;

		if (data != null) {
			resultConsumer.accept(renderType, currentShade, data);
			data.close();
		}
	}
}
