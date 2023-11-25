package com.jozufozu.flywheel.impl.visualization;

import org.jetbrains.annotations.NotNull;
import org.joml.FrustumIntersection;
import org.joml.Matrix4f;

import com.jozufozu.flywheel.api.event.RenderContext;

import net.minecraft.core.Vec3i;

public record FrameContext(double cameraX, double cameraY, double cameraZ, FrustumIntersection frustum, float partialTick) {
	@NotNull
	public static FrameContext create(RenderContext context, Vec3i renderOrigin) {
		var cameraPos = context.camera()
				.getPosition();
		double cameraX = cameraPos.x;
		double cameraY = cameraPos.y;
		double cameraZ = cameraPos.z;

		Matrix4f viewProjection = new Matrix4f(context.viewProjection());
		viewProjection.translate((float) (renderOrigin.getX() - cameraX), (float) (renderOrigin.getY() - cameraY), (float) (renderOrigin.getZ() - cameraZ));
		FrustumIntersection frustum = new FrustumIntersection(viewProjection);

		return new FrameContext(cameraX, cameraY, cameraZ, frustum, context.partialTick());
	}
}
