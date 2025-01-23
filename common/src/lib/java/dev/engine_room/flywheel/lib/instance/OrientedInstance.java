package dev.engine_room.flywheel.lib.instance;

import org.joml.Quaternionf;
import org.joml.Quaternionfc;
import org.joml.Vector3fc;

import dev.engine_room.flywheel.api.instance.InstanceHandle;
import dev.engine_room.flywheel.api.instance.InstanceType;
import dev.engine_room.flywheel.lib.transform.Rotate;
import net.minecraft.core.Vec3i;
import net.minecraft.world.phys.Vec3;

public class OrientedInstance extends ColoredLitOverlayInstance implements Rotate<OrientedInstance> {
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

	public OrientedInstance position(float x, float y, float z) {
		posX = x;
		posY = y;
		posZ = z;
		return this;
	}

	public OrientedInstance position(Vector3fc pos) {
		return position(pos.x(), pos.y(), pos.z());
	}

	public OrientedInstance position(Vec3i pos) {
		return position(pos.getX(), pos.getY(), pos.getZ());
	}

	public OrientedInstance position(Vec3 pos) {
		return position((float) pos.x(), (float) pos.y(), (float) pos.z());
	}

	public OrientedInstance zeroPosition() {
		return position(0, 0, 0);
	}

	public OrientedInstance translatePosition(float x, float y, float z) {
		posX += x;
		posY += y;
		posZ += z;
		return this;
	}

	public OrientedInstance pivot(float x, float y, float z) {
		pivotX = x;
		pivotY = y;
		pivotZ = z;
		return this;
	}

	public OrientedInstance pivot(Vector3fc pos) {
		return pivot(pos.x(), pos.y(), pos.z());
	}

	public OrientedInstance pivot(Vec3i pos) {
		return pivot(pos.getX(), pos.getY(), pos.getZ());
	}

	public OrientedInstance pivot(Vec3 pos) {
		return pivot((float) pos.x(), (float) pos.y(), (float) pos.z());
	}

	public OrientedInstance centerPivot() {
		return pivot(0.5f, 0.5f, 0.5f);
	}

	public OrientedInstance translatePivot(float x, float y, float z) {
		pivotX += x;
		pivotY += y;
		pivotZ += z;
		return this;
	}

	public OrientedInstance rotation(Quaternionfc q) {
		 rotation.set(q);
		 return this;
	}

	public OrientedInstance rotation(float x, float y, float z, float w) {
		rotation.set(x, y, z, w);
		return this;
	}

	public OrientedInstance identityRotation() {
		rotation.identity();
		return this;
	}

	@Override
	public OrientedInstance rotate(Quaternionfc quaternion) {
		rotation.mul(quaternion);
		return this;
	}
}
