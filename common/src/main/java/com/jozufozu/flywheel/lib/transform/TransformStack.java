package com.jozufozu.flywheel.lib.transform;

import com.jozufozu.flywheel.lib.internal.FlwLibLink;
import com.mojang.blaze3d.vertex.PoseStack;

public interface TransformStack<Self extends TransformStack<Self>> extends Transform<Self> {
	static PoseTransformStack of(PoseStack stack) {
		return FlwLibLink.INSTANCE.getPoseTransformStackOf(stack);
	}

	Self pushPose();

	Self popPose();
}
