package com.jozufozu.flywheel.backend.instancing.batching;

import java.util.HashSet;
import java.util.Set;

import com.jozufozu.flywheel.extension.RenderTypeExtension;
import com.mojang.blaze3d.vertex.BufferBuilder;

import net.minecraft.client.renderer.RenderType;

public class BatchDrawingTracker {

	protected final Set<RenderType> activeTypes = new HashSet<>();
	private final BufferBuilder scratch;

	public BatchDrawingTracker() {
		scratch = new BufferBuilder(8);

		((BufferBuilderExtension) scratch).flywheel$freeBuffer();
	}

	public DrawBuffer getBuffer(RenderType renderType) {
		activeTypes.add(renderType);
		return RenderTypeExtension.getDrawBuffer(renderType);
	}

	/**
	 * Draws all active DrawBuffers and reset them.
	 */
	public void endBatch() {
		// TODO: when/if this causes trouble with shaders, try to inject our BufferBuilders
		//  into the RenderBuffers from context.

		for (RenderType renderType : activeTypes) {
			_draw(renderType);
		}

		activeTypes.clear();
	}

	/**
	 * Draw and reset the DrawBuffer for the given RenderType.
	 * @param renderType The RenderType to draw.
	 */
	public void endBatch(RenderType renderType) {
		_draw(renderType);

		activeTypes.remove(renderType);
	}

	/**
	 * Resets all DrawBuffers to 0 vertices.
	 */
	public void clear() {
		for (RenderType type : activeTypes) {
			RenderTypeExtension.getDrawBuffer(type)
					.reset();
		}
		activeTypes.clear();
	}

	private void _draw(RenderType renderType) {
		DrawBuffer drawBuffer = RenderTypeExtension.getDrawBuffer(renderType);

		BufferBuilderExtension scratch = (BufferBuilderExtension) this.scratch;
		if (drawBuffer.hasVertices()) {
			drawBuffer.inject(scratch);

			renderType.end(this.scratch, 0, 0, 0);

			drawBuffer.reset();
		}
	}

}
