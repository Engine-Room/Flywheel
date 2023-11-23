package com.jozufozu.flywheel.lib.transform;

import org.joml.AxisAngle4f;
import org.joml.Quaternionf;
import org.joml.Vector3fc;

import com.jozufozu.flywheel.lib.util.Axes;

import net.minecraft.core.Direction;

public interface Rotate<Self> {
	Self multiply(Quaternionf quaternion);

	@SuppressWarnings("unchecked")
	default Self rotate(Direction axis, float radians) {
		if (radians == 0)
			return (Self) this;
		return multiplyRadians(axis.step(), radians);
	}

	default Self rotate(double angle, Direction.Axis axis) {
        Axes.Axis vec = switch (axis) {
            case X -> Axes.XP;
			case Y -> Axes.YP;
			case Z -> Axes.ZP;
        };
        return multiply(vec.vec(), angle);
	}

	default Self rotateX(double angle) {
		return multiply(Axes.XP.vec(), angle);
	}

	default Self rotateY(double angle) {
		return multiply(Axes.YP.vec(), angle);
	}

	default Self rotateZ(double angle) {
		return multiply(Axes.ZP.vec(), angle);
	}

	default Self rotateXRadians(double angle) {
		return multiplyRadians(Axes.XP.vec(), angle);
	}

	default Self rotateYRadians(double angle) {
		return multiplyRadians(Axes.YP.vec(), angle);
	}

	default Self rotateZRadians(double angle) {
		return multiplyRadians(Axes.ZP.vec(), angle);
	}

	@SuppressWarnings("unchecked")
	default Self multiply(Vector3fc axis, double angle) {
		if (angle == 0)
			return (Self) this;
		return multiplyRadians(axis, Math.toRadians(angle));
	}

	@SuppressWarnings("unchecked")
	default Self multiplyRadians(Vector3fc axis, double angle) {
		if (angle == 0)
			return (Self) this;
		return multiply(new Quaternionf(new AxisAngle4f((float) angle, axis)));
	}

	@SuppressWarnings("unchecked")
	default Self rotateToFace(Direction facing) {
		switch (facing) {
		case SOUTH -> multiply(Axes.YP.rotationDegrees(180));
		case WEST -> multiply(Axes.YP.rotationDegrees(90));
		case NORTH -> multiply(Axes.YP.rotationDegrees(0));
		case EAST -> multiply(Axes.YP.rotationDegrees(270));
		case UP -> multiply(Axes.XP.rotationDegrees(90));
		case DOWN -> multiply(Axes.XN.rotationDegrees(90));
		}
		return (Self) this;
	}
}
