package com.jozufozu.flywheel.core;

import org.jetbrains.annotations.NotNull;

import com.jozufozu.flywheel.util.extension.Matrix4fExtension;
import com.jozufozu.flywheel.util.joml.FrustumIntersection;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Matrix4f;

import net.minecraft.client.Camera;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.RenderBuffers;
import net.minecraft.world.phys.Vec3;

public record RenderContext(LevelRenderer renderer, ClientLevel level, PoseStack stack, Matrix4f viewProjection,
							Matrix4f projection, RenderBuffers buffers, Camera camera, FrustumIntersection culler) {

	@NotNull
	public static Matrix4f createViewProjection(PoseStack view, Matrix4f projection) {
		var viewProjection = projection.copy();
		viewProjection.multiply(view.last().pose());
		return viewProjection;
	}

	public static FrustumIntersection createCuller(Camera camera, Matrix4f viewProjection) {
		com.jozufozu.flywheel.util.joml.Matrix4f proj = Matrix4fExtension.clone(viewProjection);

		Vec3 cam = camera
				.getPosition();

		proj.translate((float) -cam.x, (float) -cam.y, (float) -cam.z);

		return new FrustumIntersection(proj);
	}
}
