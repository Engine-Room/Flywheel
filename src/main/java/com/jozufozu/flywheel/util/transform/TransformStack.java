package com.jozufozu.flywheel.util.transform;

import net.minecraft.util.Direction;
import net.minecraft.util.math.vector.Quaternion;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.math.vector.Vector3f;
import net.minecraft.util.math.vector.Vector3i;

public interface TransformStack {
	Vector3d CENTER = new Vector3d(0.5, 0.5, 0.5);

	TransformStack translate(double x, double y, double z);

	TransformStack multiply(Quaternion quaternion);

	TransformStack push();

	TransformStack pop();

	default TransformStack rotate(Direction axis, float radians) {
		if (radians == 0)
			return this;
		return multiply(axis.getUnitVector()
				.getRadialQuaternion(radians));
	}

	default TransformStack rotate(double angle, Direction.Axis axis) {
		Vector3f vec =
				axis == Direction.Axis.X ? Vector3f.POSITIVE_X : axis == Direction.Axis.Y ? Vector3f.POSITIVE_Y : Vector3f.POSITIVE_Z;
		return multiply(vec, angle);
	}

	default TransformStack rotateX(double angle) {
		return multiply(Vector3f.POSITIVE_X, angle);
	}

	default TransformStack rotateY(double angle) {
		return multiply(Vector3f.POSITIVE_Y, angle);
	}

	default TransformStack rotateZ(double angle) {
		return multiply(Vector3f.POSITIVE_Z, angle);
	}

	default TransformStack centre() {
		return translate(CENTER);
	}

	default TransformStack unCentre() {
		return translateBack(CENTER);
	}

	default TransformStack translate(Vector3i vec) {
		return translate(vec.getX(), vec.getY(), vec.getZ());
	}

	default TransformStack translate(Vector3d vec) {
		return translate(vec.x, vec.y, vec.z);
	}

	default TransformStack translateBack(Vector3d vec) {
		return translate(-vec.x, -vec.y, -vec.z);
	}

	default TransformStack nudge(int id) {
		long randomBits = (long) id * 31L * 493286711L;
		randomBits = randomBits * randomBits * 4392167121L + randomBits * 98761L;
		float xNudge = (((float) (randomBits >> 16 & 7L) + 0.5F) / 8.0F - 0.5F) * 0.004F;
		float yNudge = (((float) (randomBits >> 20 & 7L) + 0.5F) / 8.0F - 0.5F) * 0.004F;
		float zNudge = (((float) (randomBits >> 24 & 7L) + 0.5F) / 8.0F - 0.5F) * 0.004F;
		return translate(xNudge, yNudge, zNudge);
	}

	default TransformStack multiply(Vector3f axis, double angle) {
		if (angle == 0)
			return this;
		return multiply(axis.getDegreesQuaternion((float) angle));
	}
}
