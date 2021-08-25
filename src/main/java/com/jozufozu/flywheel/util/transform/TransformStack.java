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

	TransformStack scale(float factorX, float factorY, float factorZ);

	TransformStack push();

	TransformStack pop();

	default TransformStack scale(float factor) {
		return scale(factor, factor, factor);
	}

	default TransformStack rotate(Direction axis, float radians) {
		if (radians == 0)
			return this;
		return multiply(axis.step()
				.rotation(radians));
	}

	default TransformStack rotate(double angle, Direction.Axis axis) {
		Vector3f vec =
				axis == Direction.Axis.X ? Vector3f.XP : axis == Direction.Axis.Y ? Vector3f.YP : Vector3f.ZP;
		return multiply(vec, angle);
	}

	default TransformStack rotateX(double angle) {
		return multiply(Vector3f.XP, angle);
	}

	default TransformStack rotateY(double angle) {
		return multiply(Vector3f.YP, angle);
	}

	default TransformStack rotateZ(double angle) {
		return multiply(Vector3f.ZP, angle);
	}

	default TransformStack rotateXRadians(double angle) {
		return multiplyRadians(Vector3f.XP, angle);
	}

	default TransformStack rotateYRadians(double angle) {
		return multiplyRadians(Vector3f.YP, angle);
	}

	default TransformStack rotateZRadians(double angle) {
		return multiplyRadians(Vector3f.ZP, angle);
	}

	default TransformStack centre() {
		return translateAll(0.5);
	}

	default TransformStack unCentre() {
		return translateAll(-0.5);
	}

	default TransformStack translateAll(double v) {
		return translate(v, v, v);
	}

	default TransformStack translateX(double x) {
		return translate(x, 0, 0);
	}

	default TransformStack translateY(double y) {
		return translate(0, y, 0);
	}

	default TransformStack translateZ(double z) {
		return translate(0, 0, z);
	}

	default TransformStack translate(Vector3i vec) {
		return translate(vec.getX(), vec.getY(), vec.getZ());
	}

	default TransformStack translate(Vector3d vec) {
		return translate(vec.x, vec.y, vec.z);
	}

	default TransformStack translate(Vector3f vec) {
		return translate(vec.x(), vec.y(), vec.z());
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
		return multiply(axis.rotationDegrees((float) angle));
	}

	default TransformStack multiplyRadians(Vector3f axis, double angle) {
		if (angle == 0)
			return this;
		return multiply(axis.rotation((float) angle));
	}

    default TransformStack rotateToFace(Direction facing) {
		switch (facing) {
		case SOUTH:
			multiply(Vector3f.YP.rotationDegrees(180));
			break;
		case WEST:
			multiply(Vector3f.YP.rotationDegrees(90));
			break;
		case NORTH:
			multiply(Vector3f.YP.rotationDegrees(0));
			break;
		case EAST:
			multiply(Vector3f.YP.rotationDegrees(270));
			break;
		case UP:
			multiply(Vector3f.XP.rotationDegrees(90));
			break;
		case DOWN:
			multiply(Vector3f.XN.rotationDegrees(90));
			break;
		}
		return this;
	}
}
