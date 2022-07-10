
package com.jozufozu.flywheel.core.model;

import java.util.Random;

import com.mojang.blaze3d.vertex.VertexConsumer;

import net.minecraft.client.renderer.block.ModelBlockRenderer;

/**
 * An interface for objects that can "rendered" into a BufferBuilder.
 */
public interface Bufferable {
	void bufferInto(ModelBlockRenderer renderer, VertexConsumer consumer, Random random);

	default ShadeSeparatedBufferBuilder build() {
		return ModelUtil.getBufferBuilder(this);
	}
}
