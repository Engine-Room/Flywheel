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

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.Vec3i;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.MinecraftForge;

public class ViewProvider implements UniformProvider {

	public static final int SIZE = 4 * 16 + 16 + 4;

	@Override
	public int byteSize() {
		return SIZE;
	}

	@Override
	public FileResolution uniformShader() {
		return Components.Files.VIEW_UNIFORMS;
	}

	@Override
	public ActiveUniformProvider activate(long ptr) {
		return new Active(ptr);
	}

	public static class Active implements ActiveUniformProvider, Consumer<BeginFrameEvent> {
		private final long ptr;
		private boolean dirty = true;

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
			if (dirty) {
				dirty = false;
				return true;
			}
			return false;
		}

		@Override
		public void accept(BeginFrameEvent event) {
			update(event.getContext());
		}

		public void update(RenderContext context) {
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

			MatrixWrite.writeUnsafe(vp, ptr);
			MemoryUtil.memPutFloat(ptr + 64, camX);
			MemoryUtil.memPutFloat(ptr + 68, camY);
			MemoryUtil.memPutFloat(ptr + 72, camZ);
			MemoryUtil.memPutInt(ptr + 76, constantAmbientLight);

			dirty = true;
		}
	}
}
