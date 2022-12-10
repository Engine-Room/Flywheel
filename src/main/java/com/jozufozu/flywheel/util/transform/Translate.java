package com.jozufozu.flywheel.util.transform;

import org.joml.Vector3f;

import net.minecraft.core.Vec3i;
import net.minecraft.world.phys.Vec3;

public interface Translate<Self> {
	Self translate(double x, double y, double z);

	default Self centre() {
		return translateAll(0.5);
	}

	default Self unCentre() {
		return translateAll(-0.5);
	}

	default Self translateAll(double v) {
		return translate(v, v, v);
	}

	default Self translateX(double x) {
		return translate(x, 0, 0);
	}

	default Self translateY(double y) {
		return translate(0, y, 0);
	}

	default Self translateZ(double z) {
		return translate(0, 0, z);
	}

	default Self translate(Vec3i vec) {
		return translate(vec.getX(), vec.getY(), vec.getZ());
	}

	default Self translate(Vec3 vec) {
		return translate(vec.x, vec.y, vec.z);
	}

	default Self translate(Vector3f vec) {
		return translate(vec.x(), vec.y(), vec.z());
	}

	default Self translateBack(Vec3 vec) {
		return translate(-vec.x, -vec.y, -vec.z);
	}

	default Self translateBack(double x, double y, double z) {
		return translate(-x, -y, -z);
	}

	default Self translateBack(Vec3i vec) {
		return translate(-vec.getX(), -vec.getY(), -vec.getZ());
	}

	/**
	 * Translates this object randomly by a very small amount.
	 * @param seed The seed to use to generate the random offsets.
	 * @return {@code this}
	 */
	default Self nudge(int seed) {
		long randomBits = (long) seed * 31L * 493286711L;
		randomBits = randomBits * randomBits * 4392167121L + randomBits * 98761L;
		float xNudge = (((float) (randomBits >> 16 & 7L) + 0.5F) / 8.0F - 0.5F) * 0.004F;
		float yNudge = (((float) (randomBits >> 20 & 7L) + 0.5F) / 8.0F - 0.5F) * 0.004F;
		float zNudge = (((float) (randomBits >> 24 & 7L) + 0.5F) / 8.0F - 0.5F) * 0.004F;
		return translate(xNudge, yNudge, zNudge);
	}
}
