package com.jozufozu.flywheel.backend.engine.uniform;

import org.joml.Matrix4f;
import org.lwjgl.system.MemoryUtil;

import com.jozufozu.flywheel.api.event.RenderContext;
import com.jozufozu.flywheel.api.visualization.VisualizationManager;
import com.jozufozu.flywheel.lib.math.MatrixMath;

import net.minecraft.core.Vec3i;
import net.minecraft.world.phys.Vec3;

public class FrameUniforms implements UniformProvider {
	public static final int SIZE = 220;

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
		var camera = context.camera();

		Vec3 cameraPos = camera.getPosition();
		var camX = (float) (cameraPos.x - renderOrigin.getX());
		var camY = (float) (cameraPos.y - renderOrigin.getY());
		var camZ = (float) (cameraPos.z - renderOrigin.getZ());

		viewProjection.set(context.viewProjection());
		viewProjection.translate(-camX, -camY, -camZ);

		if (!Uniforms.frustumPaused || Uniforms.frustumCapture) {
			MatrixMath.writePackedFrustumPlanes(ptr, viewProjection);
			Uniforms.frustumCapture = false;
		}

		MatrixMath.writeUnsafe(viewProjection, ptr + 96);
		writeVec3(ptr + 160, camX, camY, camZ);

		var lookVector = camera.getLookVector();
		writeVec3(ptr + 176, lookVector.x, lookVector.y, lookVector.z);

		writeVec2(ptr + 192, camera.getXRot(), camera.getYRot());

		MemoryUtil.memPutInt(ptr + 200, getConstantAmbientLightFlag(context));

		writeTime(ptr + 204);
	}

	private void writeTime(long ptr) {
		int ticks = context.renderer()
				.getTicks();
		float partialTick = context.partialTick();
		float renderTicks = ticks + partialTick;
		float renderSeconds = renderTicks / 20f;

		MemoryUtil.memPutInt(ptr, ticks);
		MemoryUtil.memPutFloat(ptr + 4, partialTick);
		MemoryUtil.memPutFloat(ptr + 8, renderTicks);
		MemoryUtil.memPutFloat(ptr + 12, renderSeconds);
	}

	private static void writeVec3(long ptr, float camX, float camY, float camZ) {
		MemoryUtil.memPutFloat(ptr, camX);
		MemoryUtil.memPutFloat(ptr + 4, camY);
		MemoryUtil.memPutFloat(ptr + 8, camZ);
		MemoryUtil.memPutFloat(ptr + 12, 0f); // empty component of vec4 because we don't trust std140
	}

	private static void writeVec2(long ptr, float camX, float camY) {
		MemoryUtil.memPutFloat(ptr, camX);
		MemoryUtil.memPutFloat(ptr + 4, camY);
	}

	private static int getConstantAmbientLightFlag(RenderContext context) {
		var constantAmbientLight = context.level()
				.effects()
				.constantAmbientLight();
		return constantAmbientLight ? 1 : 0;
	}
}
