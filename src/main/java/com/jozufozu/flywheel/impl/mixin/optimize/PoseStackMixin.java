package com.jozufozu.flywheel.impl.mixin.optimize;

import java.util.ArrayDeque;
import java.util.Deque;

import org.joml.Matrix3f;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;

import com.mojang.blaze3d.vertex.PoseStack;

@Mixin(PoseStack.class)
abstract class PoseStackMixin {
	@Shadow
	@Final
	private Deque<PoseStack.Pose> poseStack;

	// Just use a second deque for recycling. There may be a more efficient container.
	@Unique
	private final Deque<PoseStack.Pose> flywheel$recycled = new ArrayDeque<>();

	/**
	 * @author Jozufozu
	 * @reason Use a recycled pose if available, should avoid almost all allocations on push.
	 */
	@Overwrite
	public void pushPose() {
		var last = poseStack.getLast();
		if (flywheel$recycled.isEmpty()) {
			// Nothing to recycle, create a new pose. This should look exactly like the un-overwritten method.
			PoseStack.Pose pose = PoseStackPoseInvoker.flywheel$create(new Matrix4f(last.pose()), new Matrix3f(last.normal()));
			poseStack.addLast(pose);
		} else {
			// Recycle a pose, no need to allocate new matrices.
			PoseStack.Pose recycled = flywheel$recycled.pop();
			recycled.pose()
					.set(last.pose());
			recycled.normal()
					.set(last.normal());
			poseStack.addLast(recycled);
		}
	}

	/**
	 * @author Jozufozu
	 * @reason Put the popped pose back into the recycling deque.
	 */
	@Overwrite
	public void popPose() {
		// Return the pose to be recycled.
		PoseStack.Pose pose = poseStack.removeLast();
		// No need to zero out the matrices, they will be overwritten before use.
		flywheel$recycled.add(pose);
	}
}
