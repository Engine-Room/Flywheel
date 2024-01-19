package com.jozufozu.flywheel.backend.engine.uniform;

import org.joml.Matrix4f;
import org.lwjgl.system.MemoryUtil;

import com.jozufozu.flywheel.api.event.RenderContext;
import com.jozufozu.flywheel.api.visualization.VisualizationManager;
import com.jozufozu.flywheel.lib.math.MatrixMath;

import net.minecraft.core.Vec3i;
import net.minecraft.world.phys.Vec3;

public class FrameUniforms implements UniformProvider {
	public static final int SIZE = 192;

	private RenderContext context;

	public int byteSize() {
		return SIZE;
	}

	private final Matrix4f viewProjection = new Matrix4f();

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

		MatrixMath.writeUnsafe(viewProjection, ptr);
		MemoryUtil.memPutFloat(ptr + 64, camX);
		MemoryUtil.memPutFloat(ptr + 68, camY);
		MemoryUtil.memPutFloat(ptr + 72, camZ);
		MemoryUtil.memPutFloat(ptr + 76, 0f); // vec4 alignment
		MemoryUtil.memPutInt(ptr + 80, getConstantAmbientLightFlag(context));

		if (!Uniforms.frustumPaused || Uniforms.frustumCapture) {
			MatrixMath.writePackedFrustumPlanes(ptr + 96, viewProjection);
			Uniforms.frustumCapture = false;
		}
	}

	private static int getConstantAmbientLightFlag(RenderContext context) {
		var constantAmbientLight = context.level()
				.effects()
				.constantAmbientLight();
		return constantAmbientLight ? 1 : 0;
	}
}
