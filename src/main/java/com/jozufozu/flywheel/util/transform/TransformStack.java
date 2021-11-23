package com.jozufozu.flywheel.util.transform;

import com.mojang.blaze3d.vertex.PoseStack;

public interface TransformStack extends Scale<TransformStack>, Translate<TransformStack>, Rotate<TransformStack>, TStack<TransformStack> {
	static TransformStack cast(PoseStack stack) {
		return (TransformStack) stack;
	}
}
