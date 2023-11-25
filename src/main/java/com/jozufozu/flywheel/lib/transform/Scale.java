package com.jozufozu.flywheel.lib.transform;

public interface Scale<Self extends Scale<Self>> {
	Self scale(float factorX, float factorY, float factorZ);

	default Self scale(float factor) {
		return scale(factor, factor, factor);
	}
}
