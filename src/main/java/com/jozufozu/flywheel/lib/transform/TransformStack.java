package com.jozufozu.flywheel.lib.transform;

import com.mojang.blaze3d.vertex.PoseStack;

public interface TransformStack extends Transform<TransformStack> {
	TransformStack pushPose();

	TransformStack popPose();

	static TransformStack of(PoseStack stack) {
		return (TransformStack) stack;
	}
}
