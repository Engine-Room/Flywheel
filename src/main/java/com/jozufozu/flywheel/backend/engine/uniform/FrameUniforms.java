package com.jozufozu.flywheel.backend.engine.uniform;

import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;
import org.joml.Vector2f;
import org.joml.Vector3f;
import org.lwjgl.system.MemoryUtil;

import com.jozufozu.flywheel.api.event.RenderContext;
import com.jozufozu.flywheel.api.visualization.VisualizationManager;
import com.jozufozu.flywheel.lib.math.MatrixMath;

import net.minecraft.Util;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public class FrameUniforms implements UniformProvider {
	public static final int SIZE = 808;
	public int debugMode;

	@Nullable
	private RenderContext context;

	private final Matrix4f view = new Matrix4f();
	private final Matrix4f viewInverse = new Matrix4f();
	private final Matrix4f viewPrev = new Matrix4f();
	private final Matrix4f projection = new Matrix4f();
	private final Matrix4f projectionInverse = new Matrix4f();
	private final Matrix4f projectionPrev = new Matrix4f();
	private final Matrix4f viewProjection = new Matrix4f();
	private final Matrix4f viewProjectionInverse = new Matrix4f();
	private final Matrix4f viewProjectionPrev = new Matrix4f();

	private final Vector3f cameraPositionPrev = new Vector3f();
	private final Vector3f cameraLookPrev = new Vector3f();
	private final Vector2f cameraRotPrev = new Vector2f();

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
			viewPrev.set(view);
			projectionPrev.set(projection);
			viewProjectionPrev.set(viewProjectionPrev);
		}
		ptr = writeMatrices(ptr);
		viewPrev.set(view);
		projectionPrev.set(projection);
		viewProjectionPrev.set(viewProjection);

		// last values for camera
		if (!lastInit) {
			cameraPositionPrev.set(camX, camY, camZ);
			cameraLookPrev.set(camera.getLookVector());
			cameraRotPrev.set(camera.getXRot(), camera.getYRot());
		}
		ptr = writeCamera(ptr, camX, camY, camZ);
		cameraPositionPrev.set(camX, camY, camZ);
		cameraLookPrev.set(camera.getLookVector());
		cameraRotPrev.set(camera.getXRot(), camera.getYRot());

		var window = Minecraft.getInstance()
				.getWindow();
		ptr = Uniforms.writeVec2(ptr, window.getWidth(), window.getHeight());

		// default line width: net.minecraft.client.renderer.RenderStateShard.LineStateShard
		MemoryUtil.memPutFloat(ptr, Math.max(2.5F, (float) window.getWidth() / 1920.0F * 2.5F));
		ptr += 4;

		MemoryUtil.memPutFloat(ptr, (float) window.getWidth() / (float) window.getHeight());
		ptr += 4;

		MemoryUtil.memPutFloat(ptr, Minecraft.getInstance().gameRenderer.getDepthFar());
		ptr += 4;

		ptr = writeTime(ptr);

		ptr = writeCameraIn(ptr);

		MemoryUtil.memPutInt(ptr, debugMode);

		lastInit = true;
	}

	private long writeMatrices(long ptr) {
		MatrixMath.writeUnsafe(view, ptr);
		MatrixMath.writeUnsafe(view.invert(viewInverse), ptr + 64);
		MatrixMath.writeUnsafe(viewPrev, ptr + 64 * 2);
		MatrixMath.writeUnsafe(projection, ptr + 64 * 3);
		MatrixMath.writeUnsafe(projection.invert(projectionInverse), ptr + 64 * 4);
		MatrixMath.writeUnsafe(projectionPrev, ptr + 64 * 5);
		MatrixMath.writeUnsafe(viewProjection, ptr + 64 * 6);
		MatrixMath.writeUnsafe(viewProjection.invert(viewProjectionInverse), ptr + 64 * 7);
		MatrixMath.writeUnsafe(viewProjectionPrev, ptr + 64 * 8);
		return ptr + 64 * 9;
	}

	private long writeCamera(long ptr, float camX, float camY, float camZ) {
		Camera camera = context.camera();
		Vector3f lookVector = camera.getLookVector();

		ptr = Uniforms.writeVec3(ptr, camX, camY, camZ);
		ptr = Uniforms.writeVec3(ptr, cameraPositionPrev.x, cameraPositionPrev.y, cameraPositionPrev.z);

		ptr = Uniforms.writeVec3(ptr, lookVector.x, lookVector.y, lookVector.z);
		ptr = Uniforms.writeVec3(ptr, cameraLookPrev.x, cameraLookPrev.y, cameraLookPrev.z);

		ptr = Uniforms.writeVec2(ptr, camera.getXRot(), camera.getYRot());
		ptr = Uniforms.writeVec2(ptr, cameraRotPrev.x, cameraRotPrev.y);
		return ptr;
	}

	private long writeTime(long ptr) {
		int ticks = context.renderer()
				.getTicks();
		float partialTick = context.partialTick();
		float renderTicks = ticks + partialTick;
		float renderSeconds = renderTicks / 20f;
		float systemSeconds = Util.getMillis() / 1000f;

		MemoryUtil.memPutInt(ptr, ticks);
		MemoryUtil.memPutFloat(ptr + 4, partialTick);
		MemoryUtil.memPutFloat(ptr + 8, renderTicks);
		MemoryUtil.memPutFloat(ptr + 12, renderSeconds);
		MemoryUtil.memPutFloat(ptr + 16, systemSeconds);
		MemoryUtil.memPutInt(ptr + 20, (int) (Util.getMillis() % Integer.MAX_VALUE));
		return ptr + 24;
	}

	private long writeCameraIn(long ptr) {
		Camera camera = context.camera();
		if (!camera.isInitialized()) {
			MemoryUtil.memPutInt(ptr, 0);
			MemoryUtil.memPutInt(ptr + 4, 0);
			return ptr + 8;
		}
		Level level = camera.getEntity().level();
		BlockPos blockPos = camera.getBlockPosition();
		Vec3 cameraPos = camera.getPosition();
		return Uniforms.writeInFluidAndBlock(ptr, level, blockPos, cameraPos);
	}
}
