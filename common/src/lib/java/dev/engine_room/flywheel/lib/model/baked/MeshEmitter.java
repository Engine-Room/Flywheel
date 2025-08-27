package dev.engine_room.flywheel.lib.model.baked;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

import org.jetbrains.annotations.UnknownNullability;

import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;

class MeshEmitter {
	private final Supplier<BufferBuilder> bufferBuilderSupplier;

	private final List<BufferBuilder> activeBufferBuilders = new ArrayList<>();
	private final List<Object> activeKeys = new ArrayList<>();

	@UnknownNullability
	ResultConsumer<Object> resultConsumer;

	private int currentIndex = -1;
	@UnknownNullability
	private BufferBuilder currentBufferBuilder;
	@UnknownNullability
	private Object currentKey;

	MeshEmitter(Supplier<BufferBuilder> bufferBuilderSupplier) {
		this.bufferBuilderSupplier = bufferBuilderSupplier;
	}

	@SuppressWarnings("unchecked")
	public void prepare(ResultConsumer<?> resultConsumer) {
		this.resultConsumer = (ResultConsumer<Object>) resultConsumer;
	}

	public void prepareForBlock() {
		currentIndex = -1;
		currentBufferBuilder = null;
		currentKey = null;
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
		currentBufferBuilder = null;
		currentKey = null;
	}

	public BufferBuilder getBuffer(Object key) {
		if (currentIndex < 0 || !key.equals(currentKey)) {
			while (true) {
				currentIndex++;

				if (currentIndex >= activeBufferBuilders.size()) {
					BufferBuilder bufferBuilder = bufferBuilderSupplier.get();
					bufferBuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.BLOCK);
					activeBufferBuilders.add(bufferBuilder);
					activeKeys.add(key);
					currentBufferBuilder = bufferBuilder;
					currentKey = key;
					break;
				}

				Object searchKey = activeKeys.get(currentIndex);
				if (key.equals(searchKey)) {
					currentBufferBuilder = activeBufferBuilders.get(currentIndex);
					currentKey = searchKey;
					break;
				}
			}
		}

		return currentBufferBuilder;
	}
}
