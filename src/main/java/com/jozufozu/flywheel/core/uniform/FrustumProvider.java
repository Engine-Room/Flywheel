package com.jozufozu.flywheel.core.uniform;

import java.util.function.Consumer;

import org.lwjgl.system.MemoryUtil;

import com.jozufozu.flywheel.api.uniform.UniformProvider;
import com.jozufozu.flywheel.backend.instancing.InstancedRenderDispatcher;
import com.jozufozu.flywheel.core.Components;
import com.jozufozu.flywheel.core.RenderContext;
import com.jozufozu.flywheel.core.source.FileResolution;
import com.jozufozu.flywheel.event.BeginFrameEvent;

import net.minecraft.core.Vec3i;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.MinecraftForge;

public class FrustumProvider implements UniformProvider {

	public static boolean PAUSED = false;
	public static boolean CAPTURE = false;

	@Override
	public int byteSize() {
		return 96;
	}

	@Override
	public FileResolution uniformShader() {
		return Components.Files.FRUSTUM_UNIFORMS;
	}

	@Override
	public ActiveUniformProvider activate(long ptr, Notifier notifier) {
		return new Active(ptr, notifier);
	}

	static class Active implements ActiveUniformProvider, Consumer<BeginFrameEvent> {

		private final long ptr;
		private final Notifier notifier;

		public Active(long ptr, Notifier notifier) {
			this.ptr = ptr;
			this.notifier = notifier;
			MinecraftForge.EVENT_BUS.addListener(this);
		}

		@Override
		public void delete() {
			MinecraftForge.EVENT_BUS.unregister(this);
		}

		@Override
		public void poll() {

		}

		@Override
		public void accept(BeginFrameEvent event) {
			update(event.getContext());
		}

		public void update(RenderContext context) {
			if (ptr == MemoryUtil.NULL || (PAUSED && !CAPTURE)) {
				return;
			}

			Vec3i originCoordinate = InstancedRenderDispatcher.getOriginCoordinate(context.level());
			Vec3 camera = context.camera()
					.getPosition();

			var camX = (float) (camera.x - originCoordinate.getX());
			var camY = (float) (camera.y - originCoordinate.getY());
			var camZ = (float) (camera.z - originCoordinate.getZ());

			var shiftedCuller = RenderContext.createCuller(context.viewProjection(), -camX, -camY, -camZ);

			shiftedCuller.getJozuPackedPlanes(ptr);

			notifier.signalChanged();
			CAPTURE = false;
		}
	}
}
