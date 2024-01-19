package com.jozufozu.flywheel.backend.engine.uniform;

import org.joml.Matrix4f;
import org.lwjgl.system.MemoryUtil;

import com.jozufozu.flywheel.api.event.RenderContext;
import com.jozufozu.flywheel.api.visualization.VisualizationManager;
import com.jozufozu.flywheel.lib.math.MatrixMath;

import net.minecraft.core.Vec3i;
import net.minecraft.world.phys.Vec3;

public class FrameUniforms implements UniformProvider {
	public static final int SIZE = 194;

	private RenderContext context;

	private final Matrix4f viewProjection = new Matrix4f();

	public int byteSize() {
		return SIZE;
	}

	public void setContext(RenderContext context) {
		this.context = context;
	}

	@Override
	public void write(long ptr) {
		Vec3i renderOrigin = VisualizationManager.getOrThrow(context.level())
				.getRenderOrigin();
		Vec3 camera = context.camera()
				.getPosition();

		var camX = (float) (camera.x - renderOrigin.getX());
		var camY = (float) (camera.y - renderOrigin.getY());
		var camZ = (float) (camera.z - renderOrigin.getZ());

		viewProjection.set(context.viewProjection());
		viewProjection.translate(-camX, -camY, -camZ);

		if (!Uniforms.frustumPaused || Uniforms.frustumCapture) {
			MatrixMath.writePackedFrustumPlanes(ptr, viewProjection);
			Uniforms.frustumCapture = false;
		}

		MatrixMath.writeUnsafe(viewProjection, ptr + 96);
		MemoryUtil.memPutFloat(ptr + 160, camX);
		MemoryUtil.memPutFloat(ptr + 164, camY);
		MemoryUtil.memPutFloat(ptr + 168, camZ);
		MemoryUtil.memPutFloat(ptr + 172, 0f); // empty component of vec4 because we don't trust std140
		MemoryUtil.memPutInt(ptr + 176, getConstantAmbientLightFlag(context));

		int ticks = context.renderer()
				.getTicks();
		float partialTick = context.partialTick();
		float renderTicks = ticks + partialTick;
		float renderSeconds = renderTicks / 20f;

		MemoryUtil.memPutInt(ptr + 180, ticks);
		MemoryUtil.memPutFloat(ptr + 184, partialTick);
		MemoryUtil.memPutFloat(ptr + 188, renderTicks);
		MemoryUtil.memPutFloat(ptr + 192, renderSeconds);

	}

	private static int getConstantAmbientLightFlag(RenderContext context) {
		var constantAmbientLight = context.level()
				.effects()
				.constantAmbientLight();
		return constantAmbientLight ? 1 : 0;
	}
}
