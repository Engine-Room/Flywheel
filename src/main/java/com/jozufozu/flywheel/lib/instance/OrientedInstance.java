package com.jozufozu.flywheel.lib.instance;

import org.joml.Quaternionf;
import org.joml.Vector3f;

import com.jozufozu.flywheel.api.instance.InstanceHandle;
import com.jozufozu.flywheel.api.instance.InstanceType;

import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.Vec3;

public class OrientedInstance extends ColoredLitInstance {
	public float posX;
	public float posY;
	public float posZ;
	public float pivotX = 0.5f;
	public float pivotY = 0.5f;
	public float pivotZ = 0.5f;
	public final Quaternionf rotation = new Quaternionf();

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
		return setPivot(pos.x(), pos.y(), pos.z());
	}

	public OrientedInstance setPivot(Vec3 pos) {
		return setPivot((float) pos.x(), (float) pos.y(), (float) pos.z());
	}

	public OrientedInstance setPivot(float x, float y, float z) {
		this.pivotX = x;
		this.pivotY = y;
		this.pivotZ = z;
		setChanged();
		return this;
	}

	public OrientedInstance setRotation(Quaternionf q) {
		 rotation.set(q);
		 setChanged();
		 return this;
	}

	public OrientedInstance setRotation(float x, float y, float z, float w) {
		rotation.set(x, y, z, w);
		setChanged();
		return this;
	}

	public OrientedInstance resetRotation() {
		rotation.identity();
		setChanged();
		return this;
	}
}
