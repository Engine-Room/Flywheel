package dev.engine_room.vanillin.item;

import org.jetbrains.annotations.UnknownNullability;

import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;

import net.minecraft.client.renderer.RenderType;

// Basically a copy of flywheel lib's private mesh emitter, but this does not consider shade.
class ItemMeshEmitter {
	private final RenderType renderType;
	private final BufferBuilder bufferBuilder;

	private @UnknownNullability ResultConsumer resultConsumer;

	ItemMeshEmitter(RenderType renderType) {
		this.renderType = renderType;
		this.bufferBuilder = new BufferBuilder(renderType.bufferSize());
	}

	public void prepare(ResultConsumer resultConsumer) {
		this.resultConsumer = resultConsumer;
	}

	public void end() {
		if (bufferBuilder.building()) {
			emit();
		}
		resultConsumer = null;
	}

	public BufferBuilder getBuffer() {
		prepareForGeometry();
		return bufferBuilder;
	}

	private void prepareForGeometry() {
		if (!bufferBuilder.building()) {
			bufferBuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.BLOCK);
		}
	}

	private void emit() {
		var renderedBuffer = bufferBuilder.endOrDiscardIfEmpty();

		if (renderedBuffer != null) {
			resultConsumer.accept(renderType, renderedBuffer);
			renderedBuffer.release();
		}
	}

	public interface ResultConsumer {
		void accept(RenderType renderType, BufferBuilder.RenderedBuffer data);
	}
}
