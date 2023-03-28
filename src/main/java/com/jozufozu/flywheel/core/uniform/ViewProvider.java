package com.jozufozu.flywheel.core.uniform;

import org.lwjgl.system.MemoryUtil;

import com.jozufozu.flywheel.api.uniform.UniformProvider;
import com.jozufozu.flywheel.backend.instancing.InstancedRenderDispatcher;
import com.jozufozu.flywheel.core.Components;
import com.jozufozu.flywheel.core.RenderContext;
import com.jozufozu.flywheel.core.source.FileResolution;
import com.jozufozu.flywheel.event.BeginFrameEvent;
import com.jozufozu.flywheel.util.MatrixUtil;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.Vec3i;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.MinecraftForge;

public class ViewProvider extends UniformProvider {

	public ViewProvider() {
		MinecraftForge.EVENT_BUS.addListener(this::beginFrame);
	}

	public void beginFrame(BeginFrameEvent event) {
		update(event.getContext());
	}

	@Override
	public int getActualByteSize() {
		return 4 * 16 + 16 + 4;
	}

	public void update(RenderContext context) {
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
		var vp = context.viewProjection().copy();
		vp.multiplyWithTranslation(-camX, -camY, -camZ);

		MatrixUtil.writeUnsafe(vp, ptr);
		MemoryUtil.memPutFloat(ptr + 64, camX);
		MemoryUtil.memPutFloat(ptr + 68, camY);
		MemoryUtil.memPutFloat(ptr + 72, camZ);
		MemoryUtil.memPutInt(ptr + 76, constantAmbientLight);

		notifier.signalChanged();
	}

	@Override
	public FileResolution getUniformShader() {
		return Components.Files.VIEW_UNIFORMS;
	}
}
