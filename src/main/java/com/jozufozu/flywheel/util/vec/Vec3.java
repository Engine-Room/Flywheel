package com.jozufozu.flywheel.util.vec;

import net.minecraft.util.math.vector.Quaternion;
import net.minecraft.util.math.vector.Vector3f;

public class Vec3 {
	public static final Vec3 NEGATIVE_X = new Vec3(-1.0F, 0.0F, 0.0F);
	public static final Vec3 POSITIVE_X = new Vec3(1.0F, 0.0F, 0.0F);
	public static final Vec3 NEGATIVE_Y = new Vec3(0.0F, -1.0F, 0.0F);
	public static final Vec3 POSITIVE_Y = new Vec3(0.0F, 1.0F, 0.0F);
	public static final Vec3 NEGATIVE_Z = new Vec3(0.0F, 0.0F, -1.0F);
	public static final Vec3 POSITIVE_Z = new Vec3(0.0F, 0.0F, 1.0F);

	private float x;
	private float y;
	private float z;

	public Vec3(float x, float y, float z) {
		this.x = x;
		this.y = y;
		this.z = z;
	}

	public float getX() {
		return x;
	}

	public float getY() {
		return y;
	}

	public float getZ() {
		return z;
	}

	public Vec3 multiply(Quaternion quat) {
		Vec4 vec4 = new Vec4(this, 1f);

		vec4.multiply(quat);

		return set(vec4.getX(), vec4.getY(), vec4.getZ());
	}

	public Vec3 copy() {
		return new Vec3(x, y, z);
	}

	public Vector3f convert() {
		return new Vector3f(x, y, z);
	}

	public Vec3 set(float x, float y, float z) {
		this.x = x;
		this.y = y;
		this.z = z;
		return this;
	}

	public Vec3 add(Vec3 v) {
		return add(v.x, v.y, v.z);
	}

	public Vec3 add(float x, float y, float z) {
		this.x += x;
		this.y += y;
		this.z += z;
		return this;
	}

	public Vec3 sub(Vec3 v) {
		return sub(v.x, v.y, v.z);
	}

	public Vec3 sub(float x, float y, float z) {
		this.x -= x;
		this.y -= y;
		this.z -= z;
		return this;
	}
}
