package com.jozufozu.flywheel.backend.instancing.batching;

import java.util.EnumMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.jozufozu.flywheel.api.RenderStage;
import com.jozufozu.flywheel.extension.BufferBuilderExtension;
import com.jozufozu.flywheel.extension.RenderTypeExtension;
import com.mojang.blaze3d.vertex.BufferBuilder;

import net.minecraft.client.renderer.RenderType;

public class BatchingDrawTracker {
	private static final RenderStage[] RENDER_STAGES = RenderStage.values();

	private final Map<RenderStage, Set<DrawBuffer>> activeBuffers = new EnumMap<>(RenderStage.class);
	{
		for (RenderStage stage : RENDER_STAGES) {
			activeBuffers.put(stage, new HashSet<>());
		}
	}

	private final BufferBuilder scratch;

	public BatchingDrawTracker() {
		scratch = new BufferBuilder(8);

		((BufferBuilderExtension) scratch).flywheel$freeBuffer();
	}

	public DrawBuffer getBuffer(RenderType renderType, RenderStage stage) {
		DrawBuffer buffer = RenderTypeExtension.getDrawBufferSet(renderType).getBuffer(stage);
		activeBuffers.get(stage).add(buffer);
		return buffer;
	}

	/**
	 * Draw and reset all DrawBuffers for the given RenderStage.
	 * @param stage The RenderStage to draw.
	 */
	public void draw(RenderStage stage) {
		Set<DrawBuffer> buffers = activeBuffers.get(stage);
		for (DrawBuffer buffer : buffers) {
			_draw(buffer);
			buffer.reset();
		}
		buffers.clear();
	}

	/**
	 * Draw and reset all active DrawBuffers.
	 */
	public void drawAll() {
		for (Set<DrawBuffer> buffers : activeBuffers.values()) {
			for (DrawBuffer buffer : buffers) {
				_draw(buffer);
				buffer.reset();
			}
			buffers.clear();
		}
	}

	private void _draw(DrawBuffer buffer) {
		if (buffer.hasVertices()) {
			BufferBuilderExtension scratch = (BufferBuilderExtension) this.scratch;
			buffer.inject(scratch);
			buffer.getRenderType().end(this.scratch, 0, 0, 0);
		}
	}

	/**
	 * Reset all active DrawBuffers.
	 */
	public void reset() {
		for (Set<DrawBuffer> buffers : activeBuffers.values()) {
			for (DrawBuffer buffer : buffers) {
				buffer.reset();
			}
			buffers.clear();
		}
	}
}
