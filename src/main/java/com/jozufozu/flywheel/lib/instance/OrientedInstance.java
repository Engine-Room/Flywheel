package com.jozufozu.flywheel.lib.instance;

import org.joml.Quaternionf;
import org.joml.Vector3f;

import com.jozufozu.flywheel.api.instance.InstanceHandle;
import com.jozufozu.flywheel.api.instance.InstanceType;
import com.jozufozu.flywheel.lib.transform.Rotate;

import net.minecraft.core.Vec3i;
import net.minecraft.world.phys.Vec3;

public class OrientedInstance extends ColoredLitInstance implements Rotate<OrientedInstance> {
	public final Quaternionf rotation = new Quaternionf();
	public float pivotX = 0.5f;
	public float pivotY = 0.5f;
	public float pivotZ = 0.5f;
	public float posX;
	public float posY;
	public float posZ;

	public OrientedInstance(InstanceType<? extends OrientedInstance> type, InstanceHandle handle) {
		super(type, handle);
	}

	@Override
	public OrientedInstance rotate(Quaternionf quaternion) {
		rotation.mul(quaternion);
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

	public OrientedInstance setPivot(float x, float y, float z) {
		pivotX = x;
		pivotY = y;
		pivotZ = z;
		setChanged();
		return this;
	}

	public OrientedInstance setPivot(Vector3f pos) {
		return setPivot(pos.x(), pos.y(), pos.z());
	}

	public OrientedInstance setPivot(Vec3 pos) {
		return setPivot((float) pos.x(), (float) pos.y(), (float) pos.z());
	}

	public OrientedInstance nudgePosition(float x, float y, float z) {
		posX += x;
		posY += y;
		posZ += z;
		setChanged();
		return this;
	}

	public OrientedInstance setPosition(float x, float y, float z) {
		posX = x;
		posY = y;
		posZ = z;
		setChanged();
		return this;
	}

	public OrientedInstance setPosition(Vector3f pos) {
		return setPosition(pos.x(), pos.y(), pos.z());
	}

	public OrientedInstance setPosition(Vec3i pos) {
		return setPosition(pos.getX(), pos.getY(), pos.getZ());
	}

	public OrientedInstance resetPosition() {
		return setPosition(0, 0, 0);
	}
}
