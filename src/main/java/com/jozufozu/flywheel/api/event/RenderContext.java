package com.jozufozu.flywheel.api.event;

import org.jetbrains.annotations.NotNull;
import org.joml.FrustumIntersection;

import com.jozufozu.flywheel.util.MatrixUtil;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Matrix4f;

import net.minecraft.client.Camera;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.RenderBuffers;

public record RenderContext(LevelRenderer renderer, ClientLevel level, PoseStack stack, Matrix4f viewProjection,
							Matrix4f projection, RenderBuffers buffers, Camera camera, FrustumIntersection culler) {

	@NotNull
	public static Matrix4f createViewProjection(PoseStack view, Matrix4f projection) {
		var viewProjection = projection.copy();
		viewProjection.multiply(view.last().pose());
		return viewProjection;
	}

	public static FrustumIntersection createCuller(Matrix4f viewProjection, float camX, float camY, float camZ) {
		org.joml.Matrix4f proj = MatrixUtil.toJoml(viewProjection);

		proj.translate(camX, camY, camZ);

		return new FrustumIntersection(proj);
	}
}
