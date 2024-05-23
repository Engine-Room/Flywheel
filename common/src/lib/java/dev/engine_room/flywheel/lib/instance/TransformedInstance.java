package dev.engine_room.flywheel.lib.instance;

import org.joml.Matrix3f;
import org.joml.Matrix4f;
import org.joml.Quaternionf;

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
	public TransformedInstance mulPose(Matrix4f pose) {
		this.model.mul(pose);
		return this;
	}

	@Override
	public TransformedInstance mulNormal(Matrix3f normal) {
		this.normal.mul(normal);
		return this;
	}

	@Override
	public TransformedInstance rotateAround(Quaternionf quaternion, float x, float y, float z) {
		this.model.rotateAround(quaternion, x, y, z);
		this.normal.rotate(quaternion);
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

	@Override
	public TransformedInstance rotate(Quaternionf quaternion) {
		model.rotate(quaternion);
		normal.rotate(quaternion);
		return this;
	}

	@Override
	public TransformedInstance translate(double x, double y, double z) {
		model.translate((float) x, (float) y, (float) z);
		return this;
	}

	public TransformedInstance setTransform(PoseStack stack) {
		return setTransform(stack.last());
	}

	public TransformedInstance setTransform(PoseStack.Pose pose) {
		this.model.set(pose.pose());
		this.normal.set(pose.normal());
		return this;
	}

	/**
	 * Sets the transform matrices to be all zeros.
	 *
	 * <p>
	 *     This will allow the GPU to quickly discard all geometry for this instance, effectively "turning it off".
	 * </p>
	 */
	public TransformedInstance setEmptyTransform() {
		model.zero();
		normal.zero();
		return this;
	}

	public TransformedInstance loadIdentity() {
		model.identity();
		normal.identity();
		return this;
	}
}
