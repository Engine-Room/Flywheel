package com.jozufozu.flywheel.backend.engine.uniform;

import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;
import org.lwjgl.system.MemoryUtil;

import com.jozufozu.flywheel.api.event.RenderContext;
import com.jozufozu.flywheel.api.visualization.VisualizationManager;
import com.jozufozu.flywheel.lib.math.MatrixMath;

import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.core.Vec3i;
import net.minecraft.world.phys.Vec3;

public class FrameUniforms implements UniformProvider {
	public static final int SIZE = 304;

	@Nullable
	private RenderContext context;

	private final Matrix4f viewProjection = new Matrix4f();
	private final Matrix4f viewProjectionInverse = new Matrix4f();

	public int byteSize() {
		return SIZE;
	}

	public void setContext(RenderContext context) {
		this.context = context;
	}

	@Override
	public void write(long ptr) {
		if (context == null) {
			return;
		}

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

		ptr += 96;

		ptr = writeMatrices(ptr);

		ptr = writeCamera(ptr, camX, camY, camZ, camera);

		var window = Minecraft.getInstance()
				.getWindow();
		ptr = writeVec2(ptr, window.getWidth(), window.getHeight());

		// default line width: net.minecraft.client.renderer.RenderStateShard.LineStateShard
		MemoryUtil.memPutFloat(ptr, Math.max(2.5F, (float) window.getWidth() / 1920.0F * 2.5F));
		ptr += 4;

		MemoryUtil.memPutInt(ptr, getConstantAmbientLightFlag(context));
		ptr += 4;

		writeTime(ptr);
	}

	private long writeMatrices(long ptr) {
		MatrixMath.writeUnsafe(viewProjection, ptr);
		MatrixMath.writeUnsafe(viewProjection.invert(viewProjectionInverse), ptr + 64);
		return ptr + 128;
	}

	private static long writeCamera(long ptr, float camX, float camY, float camZ, Camera camera) {
		ptr = writeVec3(ptr, camX, camY, camZ);

		var lookVector = camera.getLookVector();
		ptr = writeVec3(ptr, lookVector.x, lookVector.y, lookVector.z);

		ptr = writeVec2(ptr, camera.getXRot(), camera.getYRot());
		return ptr;
	}

	private long writeTime(long ptr) {
		int ticks = context.renderer()
				.getTicks();
		float partialTick = context.partialTick();
		float renderTicks = ticks + partialTick;
		float renderSeconds = renderTicks / 20f;

		MemoryUtil.memPutInt(ptr, ticks);
		MemoryUtil.memPutFloat(ptr + 4, partialTick);
		MemoryUtil.memPutFloat(ptr + 8, renderTicks);
		MemoryUtil.memPutFloat(ptr + 12, renderSeconds);
		return ptr + 16;
	}

	private static long writeVec3(long ptr, float camX, float camY, float camZ) {
		MemoryUtil.memPutFloat(ptr, camX);
		MemoryUtil.memPutFloat(ptr + 4, camY);
		MemoryUtil.memPutFloat(ptr + 8, camZ);
		MemoryUtil.memPutFloat(ptr + 12, 0f); // empty component of vec4 because we don't trust std140
		return ptr + 16;
	}

	private static long writeVec2(long ptr, float camX, float camY) {
		MemoryUtil.memPutFloat(ptr, camX);
		MemoryUtil.memPutFloat(ptr + 4, camY);
		return ptr + 8;
	}

	private static int getConstantAmbientLightFlag(RenderContext context) {
		var constantAmbientLight = context.level()
				.effects()
				.constantAmbientLight();
		return constantAmbientLight ? 1 : 0;
	}
}
