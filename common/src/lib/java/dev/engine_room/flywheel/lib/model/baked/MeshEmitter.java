package dev.engine_room.flywheel.lib.model.baked;

import java.util.ArrayList;
import java.util.List;

import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;

import dev.engine_room.flywheel.api.material.Material;

class MeshEmitter {
	private final BufferBuilderStack bufferBuilderStack;

	private final List<BufferBuilder> activeBufferBuilders = new ArrayList<>();
	private final List<Material> activeKeys = new ArrayList<>();

	ModelBuilderResultConsumer resultConsumer;

	private int currentIndex = -1;

	MeshEmitter(BufferBuilderStack bufferBuilderStack) {
		this.bufferBuilderStack = bufferBuilderStack;
	}

	public void prepare(ModelBuilderResultConsumer resultConsumer) {
		this.resultConsumer = resultConsumer;
	}

	public void prepareForBlock() {
		currentIndex = -1;
	}

	public void end() {
		for (int index = 0; index < activeBufferBuilders.size(); index++) {
			var renderedBuffer = activeBufferBuilders.get(index).endOrDiscardIfEmpty();

			if (renderedBuffer != null) {
				resultConsumer.accept(activeKeys.get(index), renderedBuffer);
				renderedBuffer.release();
			}
		}

		activeBufferBuilders.clear();
		activeKeys.clear();
		resultConsumer = null;
		currentIndex = -1;
	}

	public BufferBuilder getBuffer(Material key) {
		if (currentIndex < 0 || !key.equals(activeKeys.get(currentIndex))) {
			while (true) {
				currentIndex++;

				if (currentIndex >= activeBufferBuilders.size()) {
					BufferBuilder bufferBuilder = bufferBuilderStack.getOrCreateBufferBuilder();
					bufferBuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.BLOCK);
					activeBufferBuilders.add(bufferBuilder);
					activeKeys.add(key);
					break;
				}

				if (key.equals(activeKeys.get(currentIndex))) {
					break;
				}
			}
		}

		return activeBufferBuilders.get(currentIndex);
	}
}
