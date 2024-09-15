package dev.engine_room.flywheel.lib.instance;

import org.joml.Matrix3f;
import org.joml.Matrix3fc;
import org.joml.Matrix4f;
import org.joml.Matrix4fc;
import org.joml.Quaternionfc;

import com.mojang.blaze3d.vertex.PoseStack;

import dev.engine_room.flywheel.api.instance.InstanceHandle;
import dev.engine_room.flywheel.api.instance.InstanceType;
import dev.engine_room.flywheel.lib.transform.Transform;
import net.minecraft.util.Mth;

public class PosedInstance extends ColoredLitInstance implements Transform<PosedInstance> {
	public final Matrix4f pose = new Matrix4f();
	public final Matrix3f normal = new Matrix3f();

	public PosedInstance(InstanceType<? extends PosedInstance> type, InstanceHandle handle) {
		super(type, handle);
	}

	@Override
	public PosedInstance mulPose(Matrix4fc pose) {
		this.pose.mul(pose);
		return this;
	}

	@Override
	public PosedInstance mulNormal(Matrix3fc normal) {
		this.normal.mul(normal);
		return this;
	}

	@Override
	public PosedInstance rotateAround(Quaternionfc quaternion, float x, float y, float z) {
		pose.rotateAround(quaternion, x, y, z);
		normal.rotate(quaternion);
		return this;
	}

	@Override
	public PosedInstance translate(float x, float y, float z) {
		pose.translate(x, y, z);
		return this;
	}

	@Override
	public PosedInstance rotate(Quaternionfc quaternion) {
		pose.rotate(quaternion);
		normal.rotate(quaternion);
		return this;
	}

	@Override
	public PosedInstance scale(float x, float y, float z) {
		pose.scale(x, y, z);

		if (x == y && y == z) {
			if (x < 0.0f) {
				normal.scale(-1.0f);
			}

			return this;
		}

		float invX = 1.0f / x;
		float invY = 1.0f / y;
		float invZ = 1.0f / z;
		float f = Mth.fastInvCubeRoot(Math.abs(invX * invY * invZ));
		normal.scale(f * invX, f * invY, f * invZ);
		return this;
	}

	public PosedInstance setTransform(PoseStack.Pose pose) {
		this.pose.set(pose.pose());
		normal.set(pose.normal());
		return this;
	}

	public PosedInstance setTransform(PoseStack stack) {
		return setTransform(stack.last());
	}

	public PosedInstance setIdentityTransform() {
		pose.identity();
		normal.identity();
		return this;
	}

	/**
	 * Sets the transform matrices to be all zeros.
	 *
	 * <p>
	 * This will allow the GPU to quickly discard all geometry for this instance, effectively "turning it off".
	 * </p>
	 */
	public PosedInstance setZeroTransform() {
		pose.zero();
		normal.zero();
		return this;
	}
}
