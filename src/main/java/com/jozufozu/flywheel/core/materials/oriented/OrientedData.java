package com.jozufozu.flywheel.core.materials.oriented;

import com.jozufozu.flywheel.backend.gl.buffer.VecBuffer;
import com.jozufozu.flywheel.core.materials.BasicData;
import com.jozufozu.flywheel.util.vec.Vec3;
import com.mojang.math.Quaternion;
import com.mojang.math.Vector3f;

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


	public OrientedData nudge(Vec3 pos) {
		return nudge(pos.getX(), pos.getY(), pos.getZ());
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

	public OrientedData setPivot(Vec3 pos) {
		return setPivot(pos.getX(), pos.getY(), pos.getZ());
	}

	public OrientedData setPivot(float x, float y, float z) {
		this.pivotX = x;
		this.pivotY = y;
		this.pivotZ = z;
		markDirty();
		return this;
	}

	public OrientedData setRotation(Quaternion q) {
		return setRotation(q.i(), q.j(), q.k(), q.r());
	}

	public OrientedData setRotation(float x, float y, float z, float w) {
		this.qX = x;
		this.qY = y;
		this.qZ = z;
		this.qW = w;
		markDirty();
		return this;
	}

	@Override
	public void write(VecBuffer buf) {
		super.write(buf);

		buf.putFloat(posX);
		buf.putFloat(posY);
		buf.putFloat(posZ);
		buf.putFloat(pivotX);
		buf.putFloat(pivotY);
		buf.putFloat(pivotZ);
		buf.putFloat(qX);
		buf.putFloat(qY);
		buf.putFloat(qZ);
		buf.putFloat(qW);
	}
}

