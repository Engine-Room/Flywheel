package com.jozufozu.flywheel.core.uniform;

import java.util.function.Consumer;

import org.lwjgl.system.MemoryUtil;

import com.jozufozu.flywheel.api.uniform.UniformProvider;
import com.jozufozu.flywheel.backend.instancing.InstancedRenderDispatcher;
import com.jozufozu.flywheel.core.Components;
import com.jozufozu.flywheel.core.RenderContext;
import com.jozufozu.flywheel.core.source.FileResolution;
import com.jozufozu.flywheel.event.BeginFrameEvent;
import com.jozufozu.flywheel.extension.MatrixWrite;
import com.mojang.blaze3d.systems.RenderSystem;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.Vec3i;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.MinecraftForge;

public class FlwUniformProvider implements UniformProvider {

	public static final int SIZE = 224;

	public static boolean FRUSTUM_PAUSED = false;
	public static boolean FRUSTUM_CAPTURE = false;
	public static boolean FOG_UPDATE = true;

	@Override
	public int byteSize() {
		return SIZE;
	}

	@Override
	public FileResolution uniformShader() {
		return Components.Files.UNIFORMS;
	}

	@Override
	public ActiveUniformProvider activate(long ptr) {
		return new Active(ptr);
	}

	public static class Active implements ActiveUniformProvider, Consumer<BeginFrameEvent> {
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
			updateFrustum(event.getContext());
			updateView(event.getContext());
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

		public void updateFrustum(RenderContext context) {
			if (ptr == MemoryUtil.NULL || (FRUSTUM_PAUSED && !FRUSTUM_CAPTURE)) {
				return;
			}

			Vec3i originCoordinate = InstancedRenderDispatcher.getOriginCoordinate(context.level());
			Vec3 camera = context.camera()
				.getPosition();

			var camX = (float) (camera.x - originCoordinate.getX());
			var camY = (float) (camera.y - originCoordinate.getY());
			var camZ = (float) (camera.z - originCoordinate.getZ());

			var shiftedCuller = RenderContext.createCuller(context.viewProjection(), -camX, -camY, -camZ);

			shiftedCuller.getJozuPackedPlanes(ptr + 128);

			dirty = true;
			FRUSTUM_CAPTURE = false;
		}

		public void updateView(RenderContext context) {
			if (ptr == MemoryUtil.NULL) {
				return;
			}

			ClientLevel level = context.level();

			int constantAmbientLight = level.effects()
				.constantAmbientLight() ? 1 : 0;

			Vec3i originCoordinate = InstancedRenderDispatcher.getOriginCoordinate(level);
			Vec3 camera = context.camera()
				.getPosition();

			var camX = (float) (camera.x - originCoordinate.getX());
			var camY = (float) (camera.y - originCoordinate.getY());
			var camZ = (float) (camera.z - originCoordinate.getZ());

			// don't want to mutate viewProjection
			var vp = context.viewProjection()
				.copy();
			vp.multiplyWithTranslation(-camX, -camY, -camZ);

			MatrixWrite.writeUnsafe(vp, ptr + 32);
			MemoryUtil.memPutFloat(ptr + 96, camX);
			MemoryUtil.memPutFloat(ptr + 100, camY);
			MemoryUtil.memPutFloat(ptr + 104, camZ);
			MemoryUtil.memPutFloat(ptr + 108, 0f); // vec4 alignment
			MemoryUtil.memPutInt(ptr + 112, constantAmbientLight);

			dirty = true;
		}
	}
}
