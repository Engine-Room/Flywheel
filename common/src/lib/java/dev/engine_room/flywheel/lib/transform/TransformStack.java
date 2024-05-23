package dev.engine_room.flywheel.lib.transform;

import com.mojang.blaze3d.vertex.PoseStack;

import dev.engine_room.flywheel.lib.internal.FlwLibLink;

public interface TransformStack<Self extends TransformStack<Self>> extends Transform<Self> {
	static PoseTransformStack of(PoseStack stack) {
		return FlwLibLink.INSTANCE.getPoseTransformStackOf(stack);
	}

	Self pushPose();

	Self popPose();
}
