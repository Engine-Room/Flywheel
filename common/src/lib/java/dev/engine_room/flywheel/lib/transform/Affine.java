package dev.engine_room.flywheel.lib.transform;

import org.joml.Quaternionf;

import com.mojang.math.Axis;

import net.minecraft.core.Direction;

public interface Affine<Self extends Affine<Self>> extends Translate<Self>, Rotate<Self>, Scale<Self> {
	default Self rotateAround(Quaternionf quaternion, float x, float y, float z) {
		return translate(x, y, z).rotate(quaternion)
				.translate(-x, -y, -z);
	}

	default Self rotateCentered(Quaternionf q) {
		return rotateAround(q, CENTER, CENTER, CENTER);
	}

	default Self rotateCentered(float radians, Axis axis) {
		return rotateCentered(axis.rotation(radians));
	}

	default Self rotateCentered(float radians, Direction axis) {
		var step = axis.step();
		return rotateCentered(new Quaternionf().setAngleAxis(radians, step.x(), step.y(), step.z()));
	}
}
