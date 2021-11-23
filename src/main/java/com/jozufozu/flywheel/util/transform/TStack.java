package com.jozufozu.flywheel.util.transform;

public interface TStack<Self> {
	Self pushPose();

	Self popPose();
}
