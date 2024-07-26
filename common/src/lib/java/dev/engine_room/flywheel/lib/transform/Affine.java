package dev.engine_room.flywheel.lib.transform;

import org.joml.Quaternionf;
import org.joml.Quaternionfc;
import org.joml.Vector3fc;

import com.mojang.math.Axis;

import net.minecraft.core.Direction;

public interface Affine<Self extends Affine<Self>> extends Translate<Self>, Rotate<Self>, Scale<Self> {
	default Self rotateAround(Quaternionfc quaternion, float x, float y, float z) {
		return translate(x, y, z).rotate(quaternion)
				.translateBack(x, y, z);
	}

	default Self rotateAround(Quaternionfc quaternion, Vector3fc vec) {
		return translate(vec.x(), vec.y(), vec.z()).rotate(quaternion)
				.translateBack(vec.x(), vec.y(), vec.z());
	}

	default Self rotateCentered(Quaternionfc q) {
		return rotateAround(q, CENTER, CENTER, CENTER);
	}

	default Self rotateCentered(float radians, Vector3fc axis) {
		if (radians == 0) {
			return self();
		}
		return rotateCentered(new Quaternionf().setAngleAxis(radians, axis.x(), axis.y(), axis.z()));
	}

	default Self rotateCentered(float radians, Axis axis) {
		if (radians == 0) {
			return self();
		}
		return rotateCentered(axis.rotation(radians));
	}

	default Self rotateCentered(float radians, Direction axis) {
		if (radians == 0) {
			return self();
		}
		return rotateCentered(radians, axis.step());
	}
}
