package com.jozufozu.flywheel.backend.instancing;

import java.util.HashSet;
import java.util.Set;

import com.jozufozu.flywheel.backend.model.BufferBuilderExtension;
import com.jozufozu.flywheel.backend.model.DirectVertexConsumer;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.VertexSorting;

import net.minecraft.client.renderer.RenderType;

public class BatchDrawingTracker {

	protected final Set<RenderType> activeTypes = new HashSet<>();
	private final BufferBuilder scratch;

	public BatchDrawingTracker() {
		scratch = new BufferBuilder(8);

		((BufferBuilderExtension) scratch).flywheel$freeBuffer();
	}

	/**
	 * Get a direct vertex consumer for drawing the given number of vertices to the given RenderType.
	 * @param renderType The RenderType to draw to.
	 * @param vertexCount The number of vertices that will be drawn.
	 * @return A direct vertex consumer.
	 */
	public DirectVertexConsumer getDirectConsumer(RenderType renderType, int vertexCount) {
		activeTypes.add(renderType);
		return RenderTypeExtension.getDrawBuffer(renderType)
				.begin(vertexCount);
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

			renderType.end(this.scratch, VertexSorting.DISTANCE_TO_ORIGIN);

			drawBuffer.reset();
		}
	}

}
