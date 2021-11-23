package com.jozufozu.flywheel.util.transform;

public interface TransformStack extends Scale<TransformStack>, Translate<TransformStack>, Rotate<TransformStack> {
	TransformStack push();

	TransformStack pop();
}
