
package com.jozufozu.flywheel.core.model;

import java.util.Random;

import com.mojang.blaze3d.vertex.VertexConsumer;

import net.minecraft.client.renderer.block.ModelBlockRenderer;

/**
 * An interface for objects that can buffered into a VertexConsumer.
 */
public interface Bufferable {
	void bufferInto(VertexConsumer consumer, ModelBlockRenderer renderer, Random random);

	default ShadeSeparatedBufferedData build() {
		return ModelUtil.getBufferedData(this);
	}
}
