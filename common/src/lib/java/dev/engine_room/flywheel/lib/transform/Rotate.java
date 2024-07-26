package dev.engine_room.flywheel.lib.transform;

import org.joml.AxisAngle4f;
import org.joml.Quaternionf;
import org.joml.Quaternionfc;
import org.joml.Vector3fc;

import com.mojang.math.Axis;

import net.minecraft.core.Direction;

public interface Rotate<Self extends Rotate<Self>> {
	Self rotate(Quaternionfc quaternion);

	default Self rotate(AxisAngle4f axisAngle) {
		return rotate(new Quaternionf(axisAngle));
	}

	default Self rotate(float radians, Vector3fc axis) {
		if (radians == 0) {
			return self();
		}
		return rotate(new Quaternionf().setAngleAxis(radians, axis.x(), axis.y(), axis.z()));
	}

	default Self rotate(float radians, Axis axis) {
		if (radians == 0) {
			return self();
		}
		return rotate(axis.rotation(radians));
	}

	default Self rotate(float radians, Direction axis) {
		if (radians == 0) {
			return self();
		}
		return rotate(radians, axis.step());
	}

	default Self rotate(float radians, Direction.Axis axis) {
		return switch (axis) {
		case X -> rotateX(radians);
		case Y -> rotateY(radians);
		case Z -> rotateZ(radians);
		};
	}

	default Self rotateDegrees(float degrees, Vector3fc axis) {
		if (degrees == 0) {
			return self();
		}
		return rotate((float) Math.toRadians(degrees), axis);
	}

	default Self rotateDegrees(float degrees, Axis axis) {
		if (degrees == 0) {
			return self();
		}
		return rotate(axis.rotationDegrees(degrees));
	}

	default Self rotateDegrees(float degrees, Direction axis) {
		if (degrees == 0) {
			return self();
		}
		return rotate((float) Math.toRadians(degrees), axis);
	}

	default Self rotateDegrees(float degrees, Direction.Axis axis) {
		if (degrees == 0) {
			return self();
		}
		return rotate((float) Math.toRadians(degrees), axis);
	}

	default Self rotateX(float radians) {
		return rotate(radians, Axis.XP);
	}

	default Self rotateY(float radians) {
		return rotate(radians, Axis.YP);
	}

	default Self rotateZ(float radians) {
		return rotate(radians, Axis.ZP);
	}

	default Self rotateXDegrees(float degrees) {
		return rotateDegrees(degrees, Axis.XP);
	}

	default Self rotateYDegrees(float degrees) {
		return rotateDegrees(degrees, Axis.YP);
	}

	default Self rotateZDegrees(float degrees) {
		return rotateDegrees(degrees, Axis.ZP);
	}

	default Self rotateToFace(Direction facing) {
		return switch (facing) {
			case DOWN -> rotateXDegrees(-90);
			case UP -> rotateXDegrees(90);
			case NORTH -> self();
			case SOUTH -> rotateYDegrees(180);
			case WEST -> rotateYDegrees(90);
			case EAST -> rotateYDegrees(270);
		};
	}

	@SuppressWarnings("unchecked")
	default Self self() {
		return (Self) this;
	}
}
