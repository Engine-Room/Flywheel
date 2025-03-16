package dev.engine_room.vanillin.item;

import org.jetbrains.annotations.UnknownNullability;

import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.ByteBufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.MeshData;
import com.mojang.blaze3d.vertex.VertexFormat;

import net.minecraft.client.renderer.RenderType;

// Basically a copy of flywheel lib's private mesh emitter, but this does not consider shade.
class ItemMeshEmitter {
	private final RenderType renderType;
	private final ByteBufferBuilder byteBufferBuilder;
	@UnknownNullability
	private BufferBuilder bufferBuilder;

	private @UnknownNullability ResultConsumer resultConsumer;

	ItemMeshEmitter(RenderType renderType) {
		this.renderType = renderType;
		this.byteBufferBuilder = new ByteBufferBuilder(renderType.bufferSize());
	}

	public void prepare(ResultConsumer resultConsumer) {
		this.resultConsumer = resultConsumer;
	}

	public void end() {
		if (bufferBuilder != null) {
			emit();
		}
		resultConsumer = null;
	}

	public BufferBuilder getBuffer() {
		prepareForGeometry();
		return bufferBuilder;
	}

	private void prepareForGeometry() {
		if (bufferBuilder == null) {
			bufferBuilder = new BufferBuilder(byteBufferBuilder, VertexFormat.Mode.QUADS, DefaultVertexFormat.BLOCK);
		}
	}

	private void emit() {
		var data = bufferBuilder.build();
		bufferBuilder = null;

		if (data != null) {
			resultConsumer.accept(renderType, data);
			data.close();
		}
	}
	public interface ResultConsumer {
		void accept(RenderType renderType, MeshData data);
	}
}

