package dev.engine_room.flywheel.lib.util;

import com.mojang.blaze3d.vertex.PoseStack;

/**
 * A {@link PoseStack} that recycles {@link PoseStack.Pose} objects.
 *
 * <p>Vanilla's {@link PoseStack} can get quite expensive to use when each game object needs to
 * maintain their own stack. In 26.2 vanilla keeps a retained pose list and moves an index through
 * it on push/pop, so this subclass is now a compatibility name for callers that still request a
 * recycling stack explicitly.
 */
public class RecyclingPoseStack extends PoseStack {
}
