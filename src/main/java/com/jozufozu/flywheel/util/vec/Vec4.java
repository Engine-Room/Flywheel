package com.jozufozu.flywheel.util.vec;

import net.minecraft.util.math.vector.Quaternion;

public class Vec4 {

	private float x;
	private float y;
	private float z;
	private float w;

	public Vec4(float x, float y, float z, float w) {
		this.x = x;
		this.y = y;
		this.z = z;
		this.w = w;
	}

	public Vec4(Vec3 vec3) {
		this(vec3, 0);
	}

	public Vec4(Vec3 vec3, float w) {
		this.x = vec3.getX();
		this.y = vec3.getY();
		this.z = vec3.getZ();
		this.w = w;
	}

	public Vec4 multiply(Quaternion quat) {
		Quaternion quaternion = new Quaternion(quat);
		quaternion.mul(new Quaternion(this.getX(), this.getY(), this.getZ(), 0.0F));
		Quaternion quaternion1 = new Quaternion(quat);
		quaternion1.conj();
		quaternion.mul(quaternion1);
		return set(quaternion.i(), quaternion.j(), quaternion.k(), this.getW());
	}

	public Vec3 xyz() {
		return new Vec3(x, y, z);
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

	public float getW() {
		return w;
	}

	public Vec4 set(float x, float y, float z, float w) {
		this.x = x;
		this.y = y;
		this.z = z;
		this.w = w;
		return this;
	}
}
