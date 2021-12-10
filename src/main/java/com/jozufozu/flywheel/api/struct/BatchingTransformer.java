package com.jozufozu.flywheel.api.struct;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;

public abstract class BatchingTransformer<S> {

	public void draw(S s, PoseStack stack, VertexConsumer consumer) {

	}
}
