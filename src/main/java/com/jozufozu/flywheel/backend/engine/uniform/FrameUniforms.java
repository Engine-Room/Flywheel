package com.jozufozu.flywheel.backend.engine.uniform;

import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;
import org.joml.Vector2f;
import org.joml.Vector3f;
import org.lwjgl.system.MemoryUtil;

import com.jozufozu.flywheel.api.event.RenderContext;
import com.jozufozu.flywheel.api.visualization.VisualizationManager;
import com.jozufozu.flywheel.lib.math.MatrixMath;

import net.minecraft.client.Minecraft;
import net.minecraft.core.Vec3i;
import net.minecraft.world.phys.Vec3;

public class FrameUniforms implements UniformProvider {
	public static final int SIZE = 304;
	public int debugMode;

	@Nullable
	private RenderContext context;

	private final Matrix4f view = new Matrix4f();
	private final Matrix4f viewInverse = new Matrix4f();
	private final Matrix4f lastView = new Matrix4f();
	private final Matrix4f projection = new Matrix4f();
	private final Matrix4f projectionInverse = new Matrix4f();
	private final Matrix4f lastProjection = new Matrix4f();
	private final Matrix4f viewProjection = new Matrix4f();
	private final Matrix4f viewProjectionInverse = new Matrix4f();
	private final Matrix4f lastViewProjection = new Matrix4f();

	private final Vector3f lastCameraPosition = new Vector3f();
	private final Vector3f lastCameraLook = new Vector3f();
	private final Vector2f lastCameraRot = new Vector2f();

	private boolean lastInit = false;

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

		view.set(context.stack().last().pose());
		view.translate(-camX, -camY, -camZ);
		projection.set(context.projection());
		viewProjection.set(context.viewProjection());
		viewProjection.translate(-camX, -camY, -camZ);

		if (!Uniforms.frustumPaused || Uniforms.frustumCapture) {
			MatrixMath.writePackedFrustumPlanes(ptr, viewProjection);
			Uniforms.frustumCapture = false;
		}

		ptr += 96;

		// manage last values of matrices
		if (!lastInit) {
			lastView.set(view);
			lastProjection.set(projection);
			lastViewProjection.set(lastViewProjection);
		}
		ptr = writeMatrices(ptr);
		lastView.set(view);
		lastProjection.set(projection);
		lastViewProjection.set(viewProjection);

		ptr = writeCamera(ptr, camX, camY, camZ, camera.getLookVector(), camera.getXRot(), camera.getYRot());

		// last values for camera
		if (!lastInit) {
			lastCameraPosition.set(camX, camY, camZ);
			lastCameraLook.set(camera.getLookVector());
			lastCameraRot.set(camera.getXRot(), camera.getYRot());
		}
		ptr = writeCamera(ptr, lastCameraPosition.x, lastCameraPosition.y, lastCameraPosition.z, lastCameraLook,
				lastCameraRot.x, lastCameraRot.y);
		lastCameraPosition.set(camX, camY, camZ);
		lastCameraLook.set(camera.getLookVector());
		lastCameraRot.set(camera.getXRot(), camera.getYRot());

		var window = Minecraft.getInstance()
				.getWindow();
		ptr = writeVec2(ptr, window.getWidth(), window.getHeight());

		// default line width: net.minecraft.client.renderer.RenderStateShard.LineStateShard
		MemoryUtil.memPutFloat(ptr, Math.max(2.5F, (float) window.getWidth() / 1920.0F * 2.5F));
		ptr += 4;

		MemoryUtil.memPutInt(ptr, getConstantAmbientLightFlag(context));
		ptr += 4;

		ptr = writeTime(ptr);

		MemoryUtil.memPutInt(ptr, debugMode);

		lastInit = true;
	}

	private long writeMatrices(long ptr) {
		MatrixMath.writeUnsafe(view, ptr);
		MatrixMath.writeUnsafe(view.invert(viewInverse), ptr + 64);
		MatrixMath.writeUnsafe(lastView, ptr + 64 * 2);
		MatrixMath.writeUnsafe(projection, ptr + 64 * 3);
		MatrixMath.writeUnsafe(projection.invert(projectionInverse), ptr + 64 * 4);
		MatrixMath.writeUnsafe(lastProjection, ptr + 64 * 5);
		MatrixMath.writeUnsafe(viewProjection, ptr + 64 * 6);
		MatrixMath.writeUnsafe(viewProjection.invert(viewProjectionInverse), ptr + 64 * 7);
		MatrixMath.writeUnsafe(lastViewProjection, ptr + 64 * 8);
		return ptr + 64 * 9;
	}

	private static long writeCamera(long ptr, float camX, float camY, float camZ, Vector3f lookVector, float xRot,
									float yRot) {
		ptr = writeVec3(ptr, camX, camY, camZ);

		ptr = writeVec3(ptr, lookVector.x, lookVector.y, lookVector.z);

		ptr = writeVec2(ptr, xRot, yRot);
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
