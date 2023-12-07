package com.jozufozu.flywheel.lib.uniform;

import java.util.function.Consumer;

import org.joml.Matrix4f;
import org.lwjgl.system.MemoryUtil;

import com.jozufozu.flywheel.Flywheel;
import com.jozufozu.flywheel.api.event.BeginFrameEvent;
import com.jozufozu.flywheel.api.event.RenderContext;
import com.jozufozu.flywheel.api.uniform.ShaderUniforms;
import com.jozufozu.flywheel.api.visualization.VisualizationManager;
import com.jozufozu.flywheel.lib.math.MatrixMath;
import com.mojang.blaze3d.systems.RenderSystem;

import net.minecraft.core.Vec3i;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.MinecraftForge;

public class FlwShaderUniforms implements ShaderUniforms {
	public static final FlwShaderUniforms INSTANCE = ShaderUniforms.REGISTRY.registerAndGet(new FlwShaderUniforms());

	public static final ResourceLocation FILE = Flywheel.rl("uniform/flywheel.glsl");
	public static final int SIZE = 224;

	public static boolean frustumPaused = false;
	public static boolean frustumCapture = false;
	public static boolean fogUpdate = true;

	@Override
	public int byteSize() {
		return SIZE;
	}

	@Override
	public ResourceLocation uniformShader() {
		return FILE;
	}

	@Override
	public Provider activate(long ptr) {
		return new Active(ptr);
	}

	public static class Active implements Provider, Consumer<BeginFrameEvent> {
		private final long ptr;

		private boolean dirty;

		private final Matrix4f viewProjection = new Matrix4f();

		public Active(long ptr) {
			this.ptr = ptr;
			MinecraftForge.EVENT_BUS.addListener(this);
		}

		@Override
		public void delete() {
			MinecraftForge.EVENT_BUS.unregister(this);
		}

		@Override
		public boolean poll() {
			boolean updated = maybeUpdateFog();
			updated |= dirty;
			dirty = false;
			return updated;
		}

		@Override
		public void accept(BeginFrameEvent event) {
			if (ptr == MemoryUtil.NULL) {
				return;
			}
			RenderContext context = event.getContext();

			Vec3i renderOrigin = VisualizationManager.getOrThrow(context.level())
					.getRenderOrigin();
			Vec3 camera = context.camera()
					.getPosition();

			var camX = (float) (camera.x - renderOrigin.getX());
			var camY = (float) (camera.y - renderOrigin.getY());
			var camZ = (float) (camera.z - renderOrigin.getZ());

			viewProjection.set(context.viewProjection());
			viewProjection.translate(-camX, -camY, -camZ);

			MatrixMath.writeUnsafe(viewProjection, ptr + 32);
			MemoryUtil.memPutFloat(ptr + 96, camX);
			MemoryUtil.memPutFloat(ptr + 100, camY);
			MemoryUtil.memPutFloat(ptr + 104, camZ);
			MemoryUtil.memPutFloat(ptr + 108, 0f); // vec4 alignment
			MemoryUtil.memPutInt(ptr + 112, getConstantAmbientLightFlag(context));

			if (!frustumPaused || frustumCapture) {
				MatrixMath.writePackedFrustumPlanes(ptr + 128, viewProjection);
				frustumCapture = false;
			}

			dirty = true;
		}

		private static int getConstantAmbientLightFlag(RenderContext context) {
			var constantAmbientLight = context.level()
					.effects()
					.constantAmbientLight();
			return constantAmbientLight ? 1 : 0;
		}

		private boolean maybeUpdateFog() {
			if (!fogUpdate || ptr == MemoryUtil.NULL) {
				return false;
			}

			var color = RenderSystem.getShaderFogColor();

			MemoryUtil.memPutFloat(ptr, color[0]);
			MemoryUtil.memPutFloat(ptr + 4, color[1]);
			MemoryUtil.memPutFloat(ptr + 8, color[2]);
			MemoryUtil.memPutFloat(ptr + 12, color[3]);
			MemoryUtil.memPutFloat(ptr + 16, RenderSystem.getShaderFogStart());
			MemoryUtil.memPutFloat(ptr + 20, RenderSystem.getShaderFogEnd());
			MemoryUtil.memPutInt(ptr + 24, RenderSystem.getShaderFogShape()
					.getIndex());

			fogUpdate = false;

			return true;
		}

	}
}
