package com.jozufozu.flywheel.backend.engine.batching;

import java.util.EnumMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.jozufozu.flywheel.api.event.RenderStage;
import com.jozufozu.flywheel.extension.BufferBuilderExtension;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;

public class BatchedDrawTracker {
	private final Map<RenderStage, Set<DrawBuffer>> activeBuffers = new EnumMap<>(RenderStage.class);
	{
		for (RenderStage stage : RenderStage.values()) {
			activeBuffers.put(stage, new HashSet<>());
		}
	}

	private final BufferBuilder scratch;

	public BatchedDrawTracker() {
		scratch = new BufferBuilder(8);

		((BufferBuilderExtension) scratch).flywheel$freeBuffer();
	}

	public void markActive(RenderStage stage, DrawBuffer buffer) {
		synchronized (activeBuffers) {
			activeBuffers.get(stage)
					.add(buffer);
		}
	}

	// TODO: remove?
	public void markInactive(RenderStage stage, DrawBuffer buffer) {
		synchronized (activeBuffers) {
			activeBuffers.get(stage)
					.remove(buffer);
		}
	}

	public boolean hasStage(RenderStage stage) {
		synchronized (activeBuffers) {
			return !activeBuffers.get(stage)
					.isEmpty();
		}
	}

	/**
	 * Draw and reset all DrawBuffers for the given RenderStage.
	 *
	 * @param stage The RenderStage to draw.
	 */
	public void draw(RenderStage stage) {
		// This may appear jank, but flag synchronization in BatchingEngine guarantees that
		// the mapped-to Set will not be modified here. We don't have the same guarantee for
		// activeBuffers itself, so we need to synchronize to fetch the Set.

		Set<DrawBuffer> buffers;
		synchronized (activeBuffers) {
			buffers = activeBuffers.get(stage);
		}

		for (DrawBuffer buffer : buffers) {
			_draw(buffer);
		}
		buffers.clear();
	}

	public void _draw(DrawBuffer buffer) {
		if (buffer.hasVertices()) {
			BufferBuilderExtension scratch = (BufferBuilderExtension) this.scratch;
			buffer.inject(scratch);
			buffer.getRenderType()
					.end(this.scratch, RenderSystem.getVertexSorting());
		}
		buffer.reset();
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
