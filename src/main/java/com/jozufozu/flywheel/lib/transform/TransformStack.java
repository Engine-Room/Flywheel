package com.jozufozu.flywheel.lib.transform;

import com.jozufozu.flywheel.extension.PoseStackExtension;
import com.mojang.blaze3d.vertex.PoseStack;

public interface TransformStack<Self extends TransformStack<Self>> extends Transform<Self> {
	Self pushPose();

	Self popPose();

	static PoseTransformStack of(PoseStack stack) {
		return ((PoseStackExtension) stack).flywheel$transformStack();
	}
}
