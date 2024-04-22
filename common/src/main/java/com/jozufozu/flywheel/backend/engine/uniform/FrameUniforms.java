package com.jozufozu.flywheel.backend.engine.uniform;

import org.joml.Matrix4f;
import org.joml.Vector2f;
import org.joml.Vector3f;

import com.jozufozu.flywheel.api.event.RenderContext;
import com.jozufozu.flywheel.api.visualization.VisualizationManager;
import com.jozufozu.flywheel.backend.mixin.LevelRendererAccessor;
import com.jozufozu.flywheel.lib.math.MatrixMath;

import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public final class FrameUniforms extends UniformWriter {
	private static final int SIZE = 96 + 64 * 9 + 16 * 4 + 8 * 2 + 8 + 4 * 10;
	static final UniformBuffer BUFFER = new UniformBuffer(Uniforms.FRAME_INDEX, SIZE);

	private static final Matrix4f VIEW = new Matrix4f();
	private static final Matrix4f VIEW_INVERSE = new Matrix4f();
	private static final Matrix4f VIEW_PREV = new Matrix4f();
	private static final Matrix4f PROJECTION = new Matrix4f();
	private static final Matrix4f PROJECTION_INVERSE = new Matrix4f();
	private static final Matrix4f PROJECTION_PREV = new Matrix4f();
	private static final Matrix4f VIEW_PROJECTION = new Matrix4f();
	private static final Matrix4f VIEW_PROJECTION_INVERSE = new Matrix4f();
	private static final Matrix4f VIEW_PROJECTION_PREV = new Matrix4f();

	private static final Vector3f CAMERA_POS = new Vector3f();
	private static final Vector3f CAMERA_POS_PREV = new Vector3f();
	private static final Vector3f CAMERA_LOOK = new Vector3f();
	private static final Vector3f CAMERA_LOOK_PREV = new Vector3f();
	private static final Vector2f CAMERA_ROT = new Vector2f();
	private static final Vector2f CAMERA_ROT_PREV = new Vector2f();

	private static boolean firstWrite = true;

	private static int debugMode = DebugMode.OFF.ordinal();
	private static boolean frustumPaused = false;
	private static boolean frustumCapture = false;

	private FrameUniforms() {
	}

	public static void debugMode(DebugMode mode) {
		debugMode = mode.ordinal();
	}

	public static void captureFrustum() {
		frustumPaused = true;
		frustumCapture = true;
	}

	public static void unpauseFrustum() {
		frustumPaused = false;
	}

	public static void update(RenderContext context) {
		long ptr = BUFFER.ptr();
		setPrev();

		Vec3i renderOrigin = VisualizationManager.getOrThrow(context.level())
				.getRenderOrigin();
		var camera = context.camera();
		Vec3 cameraPos = camera.getPosition();
		var camX = (float) (cameraPos.x - renderOrigin.getX());
		var camY = (float) (cameraPos.y - renderOrigin.getY());
		var camZ = (float) (cameraPos.z - renderOrigin.getZ());

		VIEW.set(context.stack().last().pose());
		VIEW.translate(-camX, -camY, -camZ);
		PROJECTION.set(context.projection());
		VIEW_PROJECTION.set(context.viewProjection());
		VIEW_PROJECTION.translate(-camX, -camY, -camZ);

		CAMERA_POS.set(camX, camY, camZ);
		CAMERA_LOOK.set(camera.getLookVector());
		CAMERA_ROT.set(camera.getXRot(), camera.getYRot());

		if (firstWrite) {
			setPrev();
		}

		if (firstWrite || !frustumPaused || frustumCapture) {
			MatrixMath.writePackedFrustumPlanes(ptr, VIEW_PROJECTION);
			frustumCapture = false;
		}

		ptr += 96;

		ptr = writeMatrices(ptr);

		ptr = writeCamera(ptr);

		var window = Minecraft.getInstance()
				.getWindow();
		ptr = writeVec2(ptr, window.getWidth(), window.getHeight());
		ptr = writeFloat(ptr, (float) window.getWidth() / (float) window.getHeight());
		// default line width: net.minecraft.client.renderer.RenderStateShard.LineStateShard
		ptr = writeFloat(ptr, Math.max(2.5F, (float) window.getWidth() / 1920.0F * 2.5F));
		ptr = writeFloat(ptr, Minecraft.getInstance().gameRenderer.getDepthFar());

		ptr = writeTime(ptr, context);

		ptr = writeCameraIn(ptr, camera);

		ptr = writeInt(ptr, debugMode);

		firstWrite = false;
		BUFFER.markDirty();
	}

	private static void setPrev() {
		VIEW_PREV.set(VIEW);
		PROJECTION_PREV.set(PROJECTION);
		VIEW_PROJECTION_PREV.set(VIEW_PROJECTION);
		CAMERA_POS_PREV.set(CAMERA_POS);
		CAMERA_LOOK_PREV.set(CAMERA_LOOK);
		CAMERA_ROT_PREV.set(CAMERA_ROT);
	}

	private static long writeMatrices(long ptr) {
		ptr = writeMat4(ptr, VIEW);
		ptr = writeMat4(ptr, VIEW.invert(VIEW_INVERSE));
		ptr = writeMat4(ptr, VIEW_PREV);
		ptr = writeMat4(ptr, PROJECTION);
		ptr = writeMat4(ptr, PROJECTION.invert(PROJECTION_INVERSE));
		ptr = writeMat4(ptr, PROJECTION_PREV);
		ptr = writeMat4(ptr, VIEW_PROJECTION);
		ptr = writeMat4(ptr, VIEW_PROJECTION.invert(VIEW_PROJECTION_INVERSE));
		ptr = writeMat4(ptr, VIEW_PROJECTION_PREV);
		return ptr;
	}

	private static long writeCamera(long ptr) {
		ptr = writeVec3(ptr, CAMERA_POS.x, CAMERA_POS.y, CAMERA_POS.z);
		ptr = writeVec3(ptr, CAMERA_POS_PREV.x, CAMERA_POS_PREV.y, CAMERA_POS_PREV.z);
		ptr = writeVec3(ptr, CAMERA_LOOK.x, CAMERA_LOOK.y, CAMERA_LOOK.z);
		ptr = writeVec3(ptr, CAMERA_LOOK_PREV.x, CAMERA_LOOK_PREV.y, CAMERA_LOOK_PREV.z);
		ptr = writeVec2(ptr, CAMERA_ROT.x, CAMERA_ROT.y);
		ptr = writeVec2(ptr, CAMERA_ROT_PREV.x, CAMERA_ROT_PREV.y);
		return ptr;
	}

	private static long writeTime(long ptr, RenderContext context) {
		int ticks = ((LevelRendererAccessor) context.renderer()).flywheel$ticks();
		float partialTick = context.partialTick();
		float renderTicks = ticks + partialTick;
		float renderSeconds = renderTicks / 20f;

		ptr = writeInt(ptr, ticks);
		ptr = writeFloat(ptr, partialTick);
		ptr = writeFloat(ptr, renderTicks);
		ptr = writeFloat(ptr, renderSeconds);
		return ptr;
	}

	private static long writeCameraIn(long ptr, Camera camera) {
		if (!camera.isInitialized()) {
			ptr = writeInt(ptr, 0);
			ptr = writeInt(ptr, 0);
			return ptr;
		}

		Level level = camera.getEntity().level();
		BlockPos blockPos = camera.getBlockPosition();
		Vec3 cameraPos = camera.getPosition();
		return writeInFluidAndBlock(ptr, level, blockPos, cameraPos);
	}
}
