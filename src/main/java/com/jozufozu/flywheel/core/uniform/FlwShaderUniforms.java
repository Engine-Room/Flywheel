package com.jozufozu.flywheel.core.uniform;

import java.util.function.Consumer;

import org.lwjgl.system.MemoryUtil;

import com.jozufozu.flywheel.api.uniform.ShaderUniforms;
import com.jozufozu.flywheel.backend.instancing.InstancedRenderDispatcher;
import com.jozufozu.flywheel.core.Components;
import com.jozufozu.flywheel.core.RenderContext;
import com.jozufozu.flywheel.event.BeginFrameEvent;
import com.jozufozu.flywheel.util.FlwUtil;
import com.jozufozu.flywheel.util.MatrixUtil;
import com.mojang.blaze3d.systems.RenderSystem;

import net.minecraft.core.Vec3i;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.MinecraftForge;

public class FlwShaderUniforms implements ShaderUniforms {

	public static final int SIZE = 224;

	public static boolean FRUSTUM_PAUSED = false;
	public static boolean FRUSTUM_CAPTURE = false;
	public static boolean FOG_UPDATE = true;

	@Override
	public int byteSize() {
		return SIZE;
	}

	@Override
	public ResourceLocation uniformShader() {
		return Components.Files.UNIFORMS;
	}

	@Override
	public Provider activate(long ptr) {
		return new Active(ptr);
	}

	public static class Active implements Provider, Consumer<BeginFrameEvent> {
		private final long ptr;

		private boolean dirty;

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

			Vec3i originCoordinate = InstancedRenderDispatcher.getOriginCoordinate(context.level());
			Vec3 camera = context.camera()
					.getPosition();

			var camX = (float) (camera.x - originCoordinate.getX());
			var camY = (float) (camera.y - originCoordinate.getY());
			var camZ = (float) (camera.z - originCoordinate.getZ());

			// don't want to mutate viewProjection
			var vp = context.viewProjection()
					.copy();
			vp.multiplyWithTranslation(-camX, -camY, -camZ);

			MatrixUtil.writeUnsafe(vp, ptr + 32);
			MemoryUtil.memPutFloat(ptr + 96, camX);
			MemoryUtil.memPutFloat(ptr + 100, camY);
			MemoryUtil.memPutFloat(ptr + 104, camZ);
			MemoryUtil.memPutFloat(ptr + 108, 0f); // vec4 alignment
			MemoryUtil.memPutInt(ptr + 112, getConstantAmbientLightFlag(context));

			updateFrustum(context, camX, camY, camZ);

			dirty = true;
		}

		private static int getConstantAmbientLightFlag(RenderContext context) {
			var constantAmbientLight = context.level()
					.effects()
					.constantAmbientLight();
			return constantAmbientLight ? 1 : 0;
		}

		private boolean maybeUpdateFog() {
			if (!FOG_UPDATE || ptr == MemoryUtil.NULL) {
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

			FOG_UPDATE = false;

			return true;
		}

		private void updateFrustum(RenderContext context, float camX, float camY, float camZ) {
			if (FRUSTUM_PAUSED && !FRUSTUM_CAPTURE) {
				return;
			}

			var projection = MatrixUtil.toJoml(context.viewProjection());
			projection.translate(-camX, -camY, -camZ);

			FlwUtil.writePackedFrustumPlanes(ptr + 128, projection);

			FRUSTUM_CAPTURE = false;
		}
	}
}
