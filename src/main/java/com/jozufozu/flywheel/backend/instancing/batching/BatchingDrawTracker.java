package com.jozufozu.flywheel.backend.instancing.batching;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import com.jozufozu.flywheel.api.RenderStage;
import com.jozufozu.flywheel.extension.BufferBuilderExtension;
import com.jozufozu.flywheel.extension.RenderTypeExtension;
import com.mojang.blaze3d.vertex.BufferBuilder;

import net.minecraft.client.renderer.RenderType;

public class BatchingDrawTracker {
	private final Set<RenderType> activeTypes = new HashSet<>();
	private final BufferBuilder scratch;

	public BatchingDrawTracker() {
		scratch = new BufferBuilder(8);

		((BufferBuilderExtension) scratch).flywheel$freeBuffer();
	}

	public DrawBuffer getBuffer(RenderType renderType, RenderStage stage) {
		return getBufferSet(renderType).getBuffer(stage);
	}

	public DrawBufferSet getBufferSet(RenderType renderType) {
		activeTypes.add(renderType);
		return RenderTypeExtension.getDrawBufferSet(renderType);
	}

	/**
	 * Draw and reset all DrawBuffers for the given RenderType.
	 * @param renderType The RenderType to draw.
	 */
	public void draw(RenderType renderType) {
		_draw(renderType);

		activeTypes.remove(renderType);
	}

	/**
	 * Draw and reset all DrawBuffers for the given RenderStage.
	 * @param stage The RenderStage to draw.
	 */
	public void draw(RenderStage stage) {
		Iterator<RenderType> iterator = activeTypes.iterator();
		while (iterator.hasNext()) {
			RenderType renderType = iterator.next();
			DrawBufferSet bufferSet = RenderTypeExtension.getDrawBufferSet(renderType);
			DrawBuffer buffer = bufferSet.deactivateBuffer(stage);
			if (buffer == null) {
				continue;
			}
			if (bufferSet.getActiveStagesView().isEmpty()) {
				iterator.remove();
			}
			_draw(buffer, renderType);
		}
	}

	/**
	 * Draw and reset all active DrawBuffers.
	 */
	public void drawAll() {
		for (RenderType renderType : activeTypes) {
			_draw(renderType);
		}

		activeTypes.clear();
	}

	private void _draw(RenderType renderType) {
		DrawBufferSet bufferSet = RenderTypeExtension.getDrawBufferSet(renderType);
		for (RenderStage stage : bufferSet.getActiveStagesView()) {
			DrawBuffer buffer = bufferSet.deactivateBuffer(stage);
			_draw(buffer, renderType);
		}
	}

	private void _draw(DrawBuffer buffer, RenderType renderType) {
		if (buffer.hasVertices()) {
			BufferBuilderExtension scratch = (BufferBuilderExtension) this.scratch;
			buffer.inject(scratch);
			renderType.end(this.scratch, 0, 0, 0);
		}

		buffer.reset();
	}

	/**
	 * Reset all active DrawBuffers.
	 */
	public void reset() {
		for (RenderType type : activeTypes) {
			RenderTypeExtension.getDrawBufferSet(type)
					.reset();
		}

		activeTypes.clear();
	}
}
