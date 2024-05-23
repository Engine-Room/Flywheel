package dev.engine_room.flywheel.lib.instance;

import org.joml.Quaternionf;
import org.joml.Vector3f;

import dev.engine_room.flywheel.api.instance.InstanceHandle;
import dev.engine_room.flywheel.api.instance.InstanceType;
import dev.engine_room.flywheel.lib.transform.Rotate;
import net.minecraft.core.Vec3i;
import net.minecraft.world.phys.Vec3;

public class OrientedInstance extends ColoredLitInstance implements Rotate<OrientedInstance> {
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

	public OrientedInstance setPosition(float x, float y, float z) {
		posX = x;
		posY = y;
		posZ = z;
		return this;
	}

	public OrientedInstance setPosition(Vector3f pos) {
		return setPosition(pos.x(), pos.y(), pos.z());
	}

	public OrientedInstance setPosition(Vec3i pos) {
		return setPosition(pos.getX(), pos.getY(), pos.getZ());
	}

	public OrientedInstance setPosition(Vec3 pos) {
		return setPosition((float) pos.x(), (float) pos.y(), (float) pos.z());
	}

	public OrientedInstance resetPosition() {
		return setPosition(0, 0, 0);
	}

	public OrientedInstance nudgePosition(float x, float y, float z) {
		posX += x;
		posY += y;
		posZ += z;
		return this;
	}

	public OrientedInstance setPivot(float x, float y, float z) {
		pivotX = x;
		pivotY = y;
		pivotZ = z;
		return this;
	}

	public OrientedInstance setPivot(Vector3f pos) {
		return setPivot(pos.x(), pos.y(), pos.z());
	}

	public OrientedInstance setPivot(Vec3i pos) {
		return setPivot(pos.getX(), pos.getY(), pos.getZ());
	}

	public OrientedInstance setPivot(Vec3 pos) {
		return setPivot((float) pos.x(), (float) pos.y(), (float) pos.z());
	}

	public OrientedInstance setRotation(Quaternionf q) {
		 rotation.set(q);
		 return this;
	}

	public OrientedInstance setRotation(float x, float y, float z, float w) {
		rotation.set(x, y, z, w);
		return this;
	}

	public OrientedInstance resetRotation() {
		rotation.identity();
		return this;
	}

	@Override
	public OrientedInstance rotate(Quaternionf quaternion) {
		rotation.mul(quaternion);
		return this;
	}
}
