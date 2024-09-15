package dev.engine_room.flywheel.lib.instance;

import org.joml.Matrix4f;
import org.joml.Quaternionfc;

import com.mojang.blaze3d.vertex.PoseStack;

import dev.engine_room.flywheel.api.instance.InstanceHandle;
import dev.engine_room.flywheel.api.instance.InstanceType;
import dev.engine_room.flywheel.lib.transform.Affine;


public class TransformedInstance extends ColoredLitInstance implements Affine<TransformedInstance> {
	public final Matrix4f model = new Matrix4f();

	public TransformedInstance(InstanceType<? extends TransformedInstance> type, InstanceHandle handle) {
		super(type, handle);
	}

	@Override
	public TransformedInstance rotateAround(Quaternionfc quaternion, float x, float y, float z) {
		model.rotateAround(quaternion, x, y, z);
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
		return this;
	}

	@Override
	public TransformedInstance scale(float x, float y, float z) {
		model.scale(x, y, z);
		return this;
	}

	public TransformedInstance setTransform(PoseStack.Pose pose) {
		model.set(pose.pose());
		return this;
	}

	public TransformedInstance setTransform(PoseStack stack) {
		return setTransform(stack.last());
	}

	public TransformedInstance setIdentityTransform() {
		model.identity();
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
		return this;
	}
}
