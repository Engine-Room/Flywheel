package com.jozufozu.flywheel.core.materials.oriented;

import org.joml.Quaternionf;
import org.joml.Vector3f;

import com.jozufozu.flywheel.core.materials.BasicData;

import net.minecraft.core.BlockPos;

public class OrientedData extends BasicData {

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

	public OrientedData setPosition(BlockPos pos) {
		return setPosition(pos.getX(), pos.getY(), pos.getZ());
	}

	public OrientedData setPosition(Vector3f pos) {
		return setPosition(pos.x(), pos.y(), pos.z());
	}

	public OrientedData setPosition(float x, float y, float z) {
		this.posX = x;
		this.posY = y;
		this.posZ = z;
		markDirty();
		return this;
	}

	public OrientedData nudge(float x, float y, float z) {
		this.posX += x;
		this.posY += y;
		this.posZ += z;
		markDirty();
		return this;
	}

	public OrientedData setPivot(Vector3f pos) {
		return setPosition(pos.x(), pos.y(), pos.z());
	}

	public OrientedData setPivot(net.minecraft.world.phys.Vec3 pos) {
		return setPosition((float) pos.x(), (float) pos.y(), (float) pos.z());
	}

	public OrientedData setPivot(float x, float y, float z) {
		this.pivotX = x;
		this.pivotY = y;
		this.pivotZ = z;
		markDirty();
		return this;
	}

	public OrientedData setRotation(Quaternionf q) {
		return setRotation(q.x(), q.y(), q.z(), q.w());
	}

	public OrientedData setRotation(float x, float y, float z, float w) {
		this.qX = x;
		this.qY = y;
		this.qZ = z;
		this.qW = w;
		markDirty();
		return this;
	}

	public OrientedData resetRotation() {
		this.qX = 0;
		this.qY = 0;
		this.qZ = 0;
		this.qW = 1;
		markDirty();
		return this;
	}

}

