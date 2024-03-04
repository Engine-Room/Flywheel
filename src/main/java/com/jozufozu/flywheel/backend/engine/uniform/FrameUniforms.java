package com.jozufozu.flywheel.backend.engine.uniform;

import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;
import org.joml.Vector2f;
import org.joml.Vector3f;
import org.lwjgl.system.MemoryUtil;

import com.jozufozu.flywheel.api.event.RenderContext;
import com.jozufozu.flywheel.api.visualization.VisualizationManager;
import com.jozufozu.flywheel.backend.mixin.GameRendererAccessor;
import com.jozufozu.flywheel.lib.math.MatrixMath;

import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.phys.Vec3;

public class FrameUniforms implements UniformProvider {
	public static final int SIZE = 1188;
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

	private final Matrix4f cleanProjection = new Matrix4f();
	private final Matrix4f cleanProjectionInverse = new Matrix4f();
	private final Matrix4f cleanProjectionPrev = new Matrix4f();
	private final Matrix4f cleanViewProjection = new Matrix4f();
	private final Matrix4f cleanViewProjectionInverse = new Matrix4f();
	private final Matrix4f cleanViewProjectionPrev = new Matrix4f();

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
		setupCleanMatrices(context.stack(), camera, context.partialTick());

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
			cleanProjectionPrev.set(cleanProjection);
			cleanViewProjectionPrev.set(cleanViewProjection);
		}
		ptr = writeMatrices(ptr);
		viewPrev.set(view);
		projectionPrev.set(projection);
		viewProjectionPrev.set(viewProjection);
		cleanProjectionPrev.set(cleanProjection);
		cleanViewProjectionPrev.set(cleanViewProjection);

		ptr = writeCamera(ptr, camX, camY, camZ, camera.getLookVector(), camera.getXRot(), camera.getYRot());

		// last values for camera
		if (!lastInit) {
			cameraPositionPrev.set(camX, camY, camZ);
			cameraLookPrev.set(camera.getLookVector());
			cameraRotPrev.set(camera.getXRot(), camera.getYRot());
		}
		ptr = writeCamera(ptr, cameraPositionPrev.x, cameraPositionPrev.y, cameraPositionPrev.z, cameraLookPrev,
				cameraRotPrev.x, cameraRotPrev.y);
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

		MemoryUtil.memPutInt(ptr, getConstantAmbientLightFlag(context));
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
		MatrixMath.writeUnsafe(cleanProjection, ptr + 64 * 9);
		MatrixMath.writeUnsafe(cleanProjection.invert(cleanProjectionInverse), ptr + 64 * 10);
		MatrixMath.writeUnsafe(cleanProjectionPrev, ptr + 64 * 11);
		MatrixMath.writeUnsafe(cleanViewProjection, ptr + 64 * 12);
		MatrixMath.writeUnsafe(cleanViewProjection.invert(cleanViewProjectionInverse), ptr + 64 * 13);
		MatrixMath.writeUnsafe(cleanViewProjectionPrev, ptr + 64 * 14);
		return ptr + 64 * 15;
	}

	private void setupCleanMatrices(PoseStack stack, Camera camera, float partialTicks) {
		Minecraft mc = Minecraft.getInstance();
		GameRenderer gr = mc.gameRenderer;
		GameRendererAccessor gra = (GameRendererAccessor) gr;

		float fov = (float) gra.flywheel$getFov(camera, partialTicks, true);

		cleanProjection.identity();

		if (gra.flywheel$getZoom() != 1.0F) {
			cleanProjection.translate(gra.flywheel$getZoomX(), -gra.flywheel$getZoomY(), 0.0F);
			cleanProjection.scale(gra.flywheel$getZoom(), gra.flywheel$getZoom(), 1.0F);
		}

		cleanProjection.mul(new Matrix4f().setPerspective(fov * ((float) Math.PI / 180F), (float) mc.getWindow().getWidth() / (float) mc.getWindow().getHeight(), 0.05F, gr.getDepthFar()));

		cleanViewProjection.set(cleanProjection).mul(stack.last().pose());
	}

	private static long writeCamera(long ptr, float camX, float camY, float camZ, Vector3f lookVector, float xRot,
									float yRot) {
		ptr = Uniforms.writeVec3(ptr, camX, camY, camZ);

		ptr = Uniforms.writeVec3(ptr, lookVector.x, lookVector.y, lookVector.z);

		ptr = Uniforms.writeVec2(ptr, xRot, yRot);
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
		FluidState fState = level.getFluidState(blockPos);
		BlockState bState = level.getBlockState(blockPos);
		float height = fState.getHeight(level, blockPos);

		if (fState.isEmpty()) {
			MemoryUtil.memPutInt(ptr, 0);
		} else if (cameraPos.y < blockPos.getY() + height) {
			if (fState.is(FluidTags.WATER)) {
				MemoryUtil.memPutInt(ptr, 1);
			} else if (fState.is(FluidTags.LAVA)) {
				MemoryUtil.memPutInt(ptr, 2);
			} else {
				MemoryUtil.memPutInt(ptr, -1);
			}
		}

		if (bState.isAir()) {
			MemoryUtil.memPutInt(ptr + 4, 0);
		} else {
			if (bState.is(Blocks.POWDER_SNOW)) {
				MemoryUtil.memPutInt(ptr + 4, 0);
			} else {
				MemoryUtil.memPutInt(ptr + 4, -1);
			}
		}

		return ptr + 8;
	}

	private static int getConstantAmbientLightFlag(RenderContext context) {
		var constantAmbientLight = context.level()
				.effects()
				.constantAmbientLight();
		return constantAmbientLight ? 1 : 0;
	}
}
