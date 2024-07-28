package dev.engine_room.flywheel.lib.transform;

import org.joml.Vector3fc;
import org.joml.Vector3ic;

import net.minecraft.core.Vec3i;
import net.minecraft.world.phys.Vec3;

public interface Translate<Self extends Translate<Self>> {
	float CENTER = 0.5f;

	Self translate(float x, float y, float z);

	default Self translate(double x, double y, double z) {
		return translate((float) x, (float) y, (float) z);
	}

	default Self translate(float v) {
		return translate(v, v, v);
	}

	default Self translateX(float x) {
		return translate(x, 0, 0);
	}

	default Self translateY(float y) {
		return translate(0, y, 0);
	}

	default Self translateZ(float z) {
		return translate(0, 0, z);
	}

	default Self translate(Vec3i vec) {
		return translate(vec.getX(), vec.getY(), vec.getZ());
	}

	default Self translate(Vector3ic vec) {
		return translate(vec.x(), vec.y(), vec.z());
	}

	default Self translate(Vector3fc vec) {
		return translate(vec.x(), vec.y(), vec.z());
	}

	default Self translate(Vec3 vec) {
		return translate(vec.x, vec.y, vec.z);
	}

	default Self translateBack(float x, float y, float z) {
		return translate(-x, -y, -z);
	}

	default Self translateBack(double x, double y, double z) {
		return translate(-x, -y, -z);
	}

	default Self translateBack(float v) {
		return translate(-v);
	}

	default Self translateBack(Vec3i vec) {
		return translateBack(vec.getX(), vec.getY(), vec.getZ());
	}

	default Self translateBack(Vector3ic vec) {
		return translateBack(vec.x(), vec.y(), vec.z());
	}

	default Self translateBack(Vector3fc vec) {
		return translateBack(vec.x(), vec.y(), vec.z());
	}

	default Self translateBack(Vec3 vec) {
		return translateBack(vec.x, vec.y, vec.z);
	}

	default Self center() {
		return translate(CENTER);
	}

	default Self uncenter() {
		return translate(-CENTER);
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
