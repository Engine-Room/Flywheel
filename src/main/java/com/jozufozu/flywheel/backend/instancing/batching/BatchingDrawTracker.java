package com.jozufozu.flywheel.backend.instancing.batching;

import java.util.HashSet;
import java.util.Set;

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

	public DrawBuffer getBuffer(RenderType renderType) {
		activeTypes.add(renderType);
		return RenderTypeExtension.getDrawBuffer(renderType);
	}

	/**
	 * Draw and reset the DrawBuffer for the given RenderType.
	 * @param renderType The RenderType to draw.
	 */
	public void draw(RenderType renderType) {
		_draw(renderType);

		activeTypes.remove(renderType);
	}

	/**
	 * Draws all active DrawBuffers and reset them.
	 */
	public void drawAll() {
		for (RenderType renderType : activeTypes) {
			_draw(renderType);
		}

		activeTypes.clear();
	}

	private void _draw(RenderType renderType) {
		DrawBuffer buffer = RenderTypeExtension.getDrawBuffer(renderType);

		if (buffer.hasVertices()) {
			BufferBuilderExtension scratch = (BufferBuilderExtension) this.scratch;
			buffer.inject(scratch);
			renderType.end(this.scratch, 0, 0, 0);
			buffer.reset();
		}
	}

	/**
	 * Resets all DrawBuffers to 0 vertices.
	 */
	public void reset() {
		for (RenderType type : activeTypes) {
			RenderTypeExtension.getDrawBuffer(type)
					.reset();
		}

		activeTypes.clear();
	}

}
