package com.jozufozu.flywheel.core.structs.oriented;

import com.jozufozu.flywheel.core.structs.ColoredLitPart;
import com.jozufozu.flywheel.core.structs.StructTypes;
import com.mojang.math.Quaternion;
import com.mojang.math.Vector3f;

import net.minecraft.core.BlockPos;

public class OrientedPart extends ColoredLitPart {

	public float posX;
	public float posY;
	public float posZ;
	public float pivotX = 0.5f;
	public float pivotY = 0.5f;
	public float pivotZ = 0.5f;
	public float qX;
	public float qY;
	public float qZ;
	public float qW = 1;

	public OrientedPart() {
		super(StructTypes.ORIENTED);
	}

	public OrientedPart setPosition(BlockPos pos) {
		return setPosition(pos.getX(), pos.getY(), pos.getZ());
	}

	public OrientedPart setPosition(Vector3f pos) {
		return setPosition(pos.x(), pos.y(), pos.z());
	}

	public OrientedPart setPosition(float x, float y, float z) {
		this.posX = x;
		this.posY = y;
		this.posZ = z;
		markDirty();
		return this;
	}

	public OrientedPart nudge(float x, float y, float z) {
		this.posX += x;
		this.posY += y;
		this.posZ += z;
		markDirty();
		return this;
	}

	public OrientedPart setPivot(Vector3f pos) {
		return setPosition(pos.x(), pos.y(), pos.z());
	}

	public OrientedPart setPivot(net.minecraft.world.phys.Vec3 pos) {
		return setPosition((float) pos.x(), (float) pos.y(), (float) pos.z());
	}

	public OrientedPart setPivot(float x, float y, float z) {
		this.pivotX = x;
		this.pivotY = y;
		this.pivotZ = z;
		markDirty();
		return this;
	}

	public OrientedPart setRotation(Quaternion q) {
		return setRotation(q.i(), q.j(), q.k(), q.r());
	}

	public OrientedPart setRotation(float x, float y, float z, float w) {
		this.qX = x;
		this.qY = y;
		this.qZ = z;
		this.qW = w;
		markDirty();
		return this;
	}

	public OrientedPart resetRotation() {
		this.qX = 0;
		this.qY = 0;
		this.qZ = 0;
		this.qW = 1;
		markDirty();
		return this;
	}

	@Override
	public OrientedPart copy() {
		var out = new OrientedPart();
		out.posX = this.posX;
		out.posY = this.posY;
		out.posZ = this.posZ;
		out.pivotX = this.pivotX;
		out.pivotY = this.pivotY;
		out.pivotZ = this.pivotZ;
		out.qX = this.qX;
		out.qY = this.qY;
		out.qZ = this.qZ;
		out.qW = this.qW;
		out.r = this.r;
		out.g = this.g;
		out.b = this.b;
		out.a = this.a;
		out.blockLight = this.blockLight;
		out.skyLight = this.skyLight;
		return out;
	}
}
