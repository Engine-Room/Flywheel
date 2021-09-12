package com.jozufozu.flywheel.core.materials;

import com.jozufozu.flywheel.backend.gl.buffer.MappedBuffer;
import com.jozufozu.flywheel.backend.instancing.GPUInstancer;
import com.jozufozu.flywheel.backend.instancing.Instancer;
import com.jozufozu.flywheel.util.vec.Vec3;

import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Quaternion;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.math.vector.Vector3f;

public class OrientedData extends BasicData {

	private float posX;
	private float posY;
	private float posZ;
	private float pivotX = 0.5f;
	private float pivotY = 0.5f;
	private float pivotZ = 0.5f;
	private float qX;
	private float qY;
	private float qZ;
	private float qW = 1;

	public OrientedData(Instancer<?> owner) {
		super(owner);
	}

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

	public OrientedData setPivot(Vector3d pos) {
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
	public void write(MappedBuffer buf) {
		super.write(buf);

		buf.putFloatArray(new float[]{posX, posY, posZ, pivotX, pivotY, pivotZ, qX, qY, qZ, qW});
	}
}

