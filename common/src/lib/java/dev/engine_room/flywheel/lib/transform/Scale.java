package dev.engine_room.flywheel.lib.transform;

public interface Scale<Self extends Scale<Self>> {
	Self scale(float factorX, float factorY, float factorZ);

	default Self scale(float factor) {
		return scale(factor, factor, factor);
	}

	default Self scaleX(float factor) {
		return scale(factor, 1, 1);
	}

	default Self scaleY(float factor) {
		return scale(1, factor, 1);
	}

	default Self scaleZ(float factor) {
		return scale(1, 1, factor);
	}
}
