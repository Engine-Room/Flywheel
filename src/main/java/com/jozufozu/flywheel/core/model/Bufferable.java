
package com.jozufozu.flywheel.core.model;

import com.mojang.blaze3d.vertex.VertexConsumer;

import net.minecraft.client.renderer.block.ModelBlockRenderer;
import net.minecraft.util.RandomSource;

/**
 * An interface for objects that can "rendered" into a BufferBuilder.
 */
public interface Bufferable {
	void bufferInto(ModelBlockRenderer renderer, VertexConsumer consumer, RandomSource random);

	default ShadeSeparatedBufferBuilder build() {
		return ModelUtil.getBufferBuilder(this);
	}
}
