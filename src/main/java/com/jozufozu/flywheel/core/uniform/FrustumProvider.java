package com.jozufozu.flywheel.core.uniform;

import org.lwjgl.system.MemoryUtil;

import com.jozufozu.flywheel.api.uniform.UniformProvider;
import com.jozufozu.flywheel.backend.instancing.InstancedRenderDispatcher;
import com.jozufozu.flywheel.core.Components;
import com.jozufozu.flywheel.core.RenderContext;
import com.jozufozu.flywheel.core.source.FileResolution;
import com.jozufozu.flywheel.event.BeginFrameEvent;
import com.jozufozu.flywheel.util.extension.MatrixExtension;
import com.jozufozu.flywheel.util.joml.FrustumIntersection;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.Vec3i;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.MinecraftForge;

public class FrustumProvider extends UniformProvider {

	public FrustumProvider() {
		MinecraftForge.EVENT_BUS.addListener(this::beginFrame);
	}

	@Override
	public int getActualByteSize() {
		return 96;
	}

	@Override
	public FileResolution getUniformShader() {
		return Components.Files.FRUSTUM_UNIFORMS;
	}

	public void beginFrame(BeginFrameEvent event) {
		update(event.getContext());
	}

	public void update(RenderContext context) {
		if (ptr == MemoryUtil.NULL) {
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
	}
}
