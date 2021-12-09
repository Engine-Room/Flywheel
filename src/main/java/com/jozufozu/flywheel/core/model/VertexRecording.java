package com.jozufozu.flywheel.core.model;

import java.util.function.Consumer;

import com.google.common.collect.ImmutableList;
import com.mojang.blaze3d.vertex.VertexConsumer;

public record VertexRecording(ImmutableList<Consumer<VertexConsumer>> recording) {

	public void replay(VertexConsumer vc) {
		for (Consumer<VertexConsumer> consumer : recording) {
			consumer.accept(vc);
		}
	}
}
