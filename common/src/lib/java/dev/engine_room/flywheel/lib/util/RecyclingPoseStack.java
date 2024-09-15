package dev.engine_room.flywheel.lib.util;

import java.util.ArrayDeque;
import java.util.Deque;

import com.mojang.blaze3d.vertex.PoseStack;

import dev.engine_room.flywheel.lib.internal.FlwLibLink;

/**
 * A {@link PoseStack} that recycles {@link PoseStack.Pose} objects.
 *
 * <p>Vanilla's {@link PoseStack} can get quite expensive to use when each game object needs to
 * maintain their own stack. This class helps alleviate memory pressure by making Pose objects
 * long-lived. Note that this means that you <em>CANNOT</em> safely store a Pose object outside
 * the RecyclingPoseStack that created it.
 */
public class RecyclingPoseStack extends PoseStack {
	private final Deque<Pose> recycleBin = new ArrayDeque<>();

	@Override
	public void pushPose() {
		if (recycleBin.isEmpty()) {
			super.pushPose();
		} else {
			var last = last();
			var recycle = recycleBin.removeLast();
			recycle.pose()
					.set(last.pose());
			recycle.normal()
					.set(last.normal());
			FlwLibLink.INSTANCE.getPoseStack(this)
					.addLast(recycle);
		}
	}

	@Override
	public void popPose() {
		recycleBin.addLast(FlwLibLink.INSTANCE.getPoseStack(this)
				.removeLast());
	}
}
