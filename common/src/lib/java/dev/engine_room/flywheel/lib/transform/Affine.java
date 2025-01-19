package dev.engine_room.flywheel.lib.transform;

import org.joml.Quaternionf;
import org.joml.Quaternionfc;
import org.joml.Vector3fc;

import com.mojang.math.Axis;

import net.minecraft.core.Direction;
import net.minecraft.util.Mth;

public interface Affine<Self extends Affine<Self>> extends Translate<Self>, Rotate<Self>, Scale<Self> {
	default Self rotateAround(Quaternionfc quaternion, float x, float y, float z) {
		return translate(x, y, z).rotate(quaternion)
				.translateBack(x, y, z);
	}

	default Self rotateAround(Quaternionfc quaternion, Vector3fc vec) {
		return rotateAround(quaternion, vec.x(), vec.y(), vec.z());
	}

	default Self rotateCentered(Quaternionfc q) {
		return rotateAround(q, CENTER, CENTER, CENTER);
	}

	default Self rotateCentered(float radians, float axisX, float axisY, float axisZ) {
		if (radians == 0) {
			return self();
		}
		return rotateCentered(new Quaternionf().setAngleAxis(radians, axisX, axisY, axisZ));
	}

	default Self rotateCentered(float radians, Axis axis) {
		if (radians == 0) {
			return self();
		}
		return rotateCentered(axis.rotation(radians));
	}

	default Self rotateCentered(float radians, Vector3fc axis) {
		return rotateCentered(radians, axis.x(), axis.y(), axis.z());
	}

	default Self rotateCentered(float radians, Direction.Axis axis) {
		return rotateCentered(radians, Direction.fromAxisAndDirection(axis, Direction.AxisDirection.POSITIVE));
	}

	default Self rotateCentered(float radians, Direction axis) {
		return rotateCentered(radians, axis.getStepX(), axis.getStepY(), axis.getStepZ());
	}

	default Self rotateCenteredDegrees(float degrees, float axisX, float axisY, float axisZ) {
		return rotateCentered(Mth.DEG_TO_RAD * degrees, axisX, axisY, axisZ);
	}

	default Self rotateCenteredDegrees(float degrees, Axis axis) {
		return rotateCentered(Mth.DEG_TO_RAD * degrees, axis);
	}

	default Self rotateCenteredDegrees(float degrees, Vector3fc axis) {
		return rotateCentered(Mth.DEG_TO_RAD * degrees, axis);
	}

	default Self rotateCenteredDegrees(float degrees, Direction axis) {
		return rotateCentered(Mth.DEG_TO_RAD * degrees, axis);
	}

	default Self rotateCenteredDegrees(float degrees, Direction.Axis axis) {
		return rotateCentered(Mth.DEG_TO_RAD * degrees, axis);
	}

	default Self rotateXCentered(float radians) {
		return rotateCentered(radians, Axis.XP);
	}

	default Self rotateYCentered(float radians) {
		return rotateCentered(radians, Axis.YP);
	}

	default Self rotateZCentered(float radians) {
		return rotateCentered(radians, Axis.ZP);
	}

	default Self rotateXCenteredDegrees(float degrees) {
		return rotateXCentered(Mth.DEG_TO_RAD * degrees);
	}

	default Self rotateYCenteredDegrees(float degrees) {
		return rotateYCentered(Mth.DEG_TO_RAD * degrees);
	}

	default Self rotateZCenteredDegrees(float degrees) {
		return rotateZCentered(Mth.DEG_TO_RAD * degrees);
	}
}
