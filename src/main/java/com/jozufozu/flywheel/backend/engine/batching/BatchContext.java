package com.jozufozu.flywheel.backend.engine.batching;

import org.jetbrains.annotations.NotNull;
import org.joml.FrustumIntersection;

import com.jozufozu.flywheel.api.event.RenderContext;
import com.jozufozu.flywheel.lib.math.MatrixUtil;
import com.jozufozu.flywheel.util.FlwUtil;
import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.Vec3;

public record BatchContext(ClientLevel level, PoseStack.Pose matrices, FrustumIntersection frustum) {
	@NotNull
	static BatchContext create(RenderContext context, BlockPos origin) {
		Vec3 cameraPos = context.camera()
				.getPosition();
		var stack = FlwUtil.copyPoseStack(context.stack());
		stack.translate(origin.getX() - cameraPos.x, origin.getY() - cameraPos.y, origin.getZ() - cameraPos.z);

		org.joml.Matrix4f proj = MatrixUtil.toJoml(context.viewProjection());
		proj.translate((float) (origin.getX() - cameraPos.x), (float) (origin.getY() - cameraPos.y), (float) (origin.getZ() - cameraPos.z));

		return new BatchContext(context.level(), stack.last(), new FrustumIntersection(proj));
	}
}
