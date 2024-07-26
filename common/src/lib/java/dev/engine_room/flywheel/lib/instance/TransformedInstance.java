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

public class TransformedInstance extends ColoredLitInstance implements Transform<TransformedInstance> {
	public final Matrix4f model = new Matrix4f();
	public final Matrix3f normal = new Matrix3f();

	public TransformedInstance(InstanceType<? extends TransformedInstance> type, InstanceHandle handle) {
		super(type, handle);
	}

	@Override
	public TransformedInstance mulPose(Matrix4fc pose) {
		this.model.mul(pose);
		return this;
	}

	@Override
	public TransformedInstance mulNormal(Matrix3fc normal) {
		this.normal.mul(normal);
		return this;
	}

	@Override
	public TransformedInstance rotateAround(Quaternionfc quaternion, float x, float y, float z) {
		model.rotateAround(quaternion, x, y, z);
		normal.rotate(quaternion);
		return this;
	}

	@Override
	public TransformedInstance translate(float x, float y, float z) {
		model.translate(x, y, z);
		return this;
	}

	@Override
	public TransformedInstance rotate(Quaternionfc quaternion) {
		model.rotate(quaternion);
		normal.rotate(quaternion);
		return this;
	}

	@Override
	public TransformedInstance scale(float x, float y, float z) {
		model.scale(x, y, z);

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

	public TransformedInstance setTransform(PoseStack.Pose pose) {
		model.set(pose.pose());
		normal.set(pose.normal());
		return this;
	}

	public TransformedInstance setTransform(PoseStack stack) {
		return setTransform(stack.last());
	}

	public TransformedInstance setIdentityTransform() {
		model.identity();
		normal.identity();
		return this;
	}

	/**
	 * Sets the transform matrices to be all zeros.
	 *
	 * <p>
	 *     This will allow the GPU to quickly discard all geometry for this instance, effectively "turning it off".
	 * </p>
	 */
	public TransformedInstance setZeroTransform() {
		model.zero();
		normal.zero();
		return this;
	}
}
