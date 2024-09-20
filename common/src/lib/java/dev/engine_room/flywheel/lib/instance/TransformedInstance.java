package dev.engine_room.flywheel.lib.instance;

import org.joml.Matrix4f;
import org.joml.Matrix4fc;
import org.joml.Quaternionfc;

import com.mojang.blaze3d.vertex.PoseStack;

import dev.engine_room.flywheel.api.instance.InstanceHandle;
import dev.engine_room.flywheel.api.instance.InstanceType;
import dev.engine_room.flywheel.lib.transform.Affine;

public class TransformedInstance extends ColoredLitInstance implements Affine<TransformedInstance> {
	public final Matrix4f pose = new Matrix4f();

	public TransformedInstance(InstanceType<? extends TransformedInstance> type, InstanceHandle handle) {
		super(type, handle);
	}

	@Override
	public TransformedInstance rotateAround(Quaternionfc quaternion, float x, float y, float z) {
		pose.rotateAround(quaternion, x, y, z);
		return this;
	}

	@Override
	public TransformedInstance translate(float x, float y, float z) {
		pose.translate(x, y, z);
		return this;
	}

	@Override
	public TransformedInstance rotate(Quaternionfc quaternion) {
		pose.rotate(quaternion);
		return this;
	}

	@Override
	public TransformedInstance scale(float x, float y, float z) {
		pose.scale(x, y, z);
		return this;
	}

	public TransformedInstance setTransform(Matrix4fc pose) {
		this.pose.set(pose);
		return this;
	}

	public TransformedInstance setTransform(PoseStack.Pose pose) {
		this.pose.set(pose.pose());
		return this;
	}

	public TransformedInstance setTransform(PoseStack stack) {
		return setTransform(stack.last());
	}

	public TransformedInstance setIdentityTransform() {
		pose.identity();
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
		pose.zero();
		return this;
	}
}
