
package com.jozufozu.flywheel.core.model;

import com.mojang.blaze3d.vertex.VertexConsumer;

import net.minecraft.client.renderer.block.ModelBlockRenderer;
import net.minecraft.util.RandomSource;

/**
 * An interface for objects that can buffered into a VertexConsumer.
 */
public interface Bufferable {
	void bufferInto(VertexConsumer consumer, ModelBlockRenderer renderer, RandomSource random);

	default ShadeSeparatedBufferedData build() {
		return ModelUtil.getBufferedData(this);
	}
}
