package com.jozufozu.flywheel.lib.instance;

import org.joml.Quaternionf;
import org.joml.Vector3f;

import com.jozufozu.flywheel.api.instance.InstanceHandle;
import com.jozufozu.flywheel.api.instance.InstanceType;

import net.minecraft.core.BlockPos;

public class OrientedInstance extends ColoredLitInstance {
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

	public OrientedInstance(InstanceType<? extends OrientedInstance> type, InstanceHandle handle) {
		super(type, handle);
	}

	public OrientedInstance setPosition(BlockPos pos) {
		return setPosition(pos.getX(), pos.getY(), pos.getZ());
	}

	public OrientedInstance setPosition(Vector3f pos) {
		return setPosition(pos.x(), pos.y(), pos.z());
	}

	public OrientedInstance setPosition(float x, float y, float z) {
		this.posX = x;
		this.posY = y;
		this.posZ = z;
		setChanged();
		return this;
	}

	public OrientedInstance nudge(float x, float y, float z) {
		this.posX += x;
		this.posY += y;
		this.posZ += z;
		setChanged();
		return this;
	}

	public OrientedInstance setPivot(Vector3f pos) {
		return setPosition(pos.x(), pos.y(), pos.z());
	}

	public OrientedInstance setPivot(net.minecraft.world.phys.Vec3 pos) {
		return setPosition((float) pos.x(), (float) pos.y(), (float) pos.z());
	}

	public OrientedInstance setPivot(float x, float y, float z) {
		this.pivotX = x;
		this.pivotY = y;
		this.pivotZ = z;
		setChanged();
		return this;
	}

	public OrientedInstance setRotation(Quaternionf q) {
		return setRotation(q.x, q.y, q.z, q.w);
	}

	public OrientedInstance setRotation(float x, float y, float z, float w) {
		this.qX = x;
		this.qY = y;
		this.qZ = z;
		this.qW = w;
		setChanged();
		return this;
	}

	public OrientedInstance resetRotation() {
		this.qX = 0;
		this.qY = 0;
		this.qZ = 0;
		this.qW = 1;
		setChanged();
		return this;
	}
}
