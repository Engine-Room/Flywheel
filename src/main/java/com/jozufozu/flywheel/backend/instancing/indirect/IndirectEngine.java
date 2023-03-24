package com.jozufozu.flywheel.backend.instancing.indirect;

import java.util.Collections;
import java.util.List;

import org.lwjgl.opengl.GL32;

import com.jozufozu.flywheel.api.RenderStage;
import com.jozufozu.flywheel.api.context.ContextShader;
import com.jozufozu.flywheel.api.instancer.InstancedPart;
import com.jozufozu.flywheel.api.instancer.Instancer;
import com.jozufozu.flywheel.api.struct.StructType;
import com.jozufozu.flywheel.backend.gl.GlStateTracker;
import com.jozufozu.flywheel.backend.gl.GlTextureUnit;
import com.jozufozu.flywheel.backend.instancing.Engine;
import com.jozufozu.flywheel.backend.instancing.InstanceManager;
import com.jozufozu.flywheel.backend.instancing.TaskEngine;
import com.jozufozu.flywheel.core.RenderContext;
import com.jozufozu.flywheel.core.model.Model;
import com.jozufozu.flywheel.util.WeakHashSet;
import com.mojang.blaze3d.systems.RenderSystem;

import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;

public class IndirectEngine implements Engine {

	protected final IndirectDrawManager drawManager = new IndirectDrawManager();

	/**
	 * The set of instance managers that are attached to this engine.
	 */
	private final WeakHashSet<InstanceManager<?>> instanceManagers = new WeakHashSet<>();

	protected final ContextShader context;
	protected final int sqrMaxOriginDistance;

	protected BlockPos originCoordinate = BlockPos.ZERO;

	public IndirectEngine(ContextShader context, int sqrMaxOriginDistance) {
		this.context = context;
		this.sqrMaxOriginDistance = sqrMaxOriginDistance;
	}

	@Override
	public <D extends InstancedPart> Instancer<D> instancer(StructType<D> type, Model model) {
		return drawManager.getInstancer(type, model);
	}

	@Override
	public void beginFrame(TaskEngine taskEngine, RenderContext context) {
		try (var restoreState = GlStateTracker.getRestoreState()) {
			drawManager.flush();
		}
	}

	@Override
	public void renderStage(TaskEngine taskEngine, RenderContext context, RenderStage stage) {
		try (var restoreState = GlStateTracker.getRestoreState()) {
			setup();

			for (var list : drawManager.renderLists.values()) {
				list.submit(stage);
			}
		}
	}

	protected void setup() {
		GlTextureUnit.T2.makeActive();
		Minecraft.getInstance().gameRenderer.lightTexture().turnOnLightLayer();

		RenderSystem.depthMask(true);
		RenderSystem.colorMask(true, true, true, true);
		RenderSystem.enableDepthTest();
		RenderSystem.depthFunc(GL32.GL_LEQUAL);
		RenderSystem.enableCull();
	}

	@Override
	public boolean maintainOriginCoordinate(Camera camera) {
		Vec3 cameraPos = camera.getPosition();

		double distanceSqr = Vec3.atLowerCornerOf(originCoordinate)
				.subtract(cameraPos)
				.lengthSqr();

		if (distanceSqr > sqrMaxOriginDistance) {
			shiftListeners(Mth.floor(cameraPos.x), Mth.floor(cameraPos.y), Mth.floor(cameraPos.z));
			return true;
		}
		return false;
	}

	private void shiftListeners(int cX, int cY, int cZ) {
		originCoordinate = new BlockPos(cX, cY, cZ);

		drawManager.clearInstancers();

		instanceManagers.forEach(InstanceManager::onOriginShift);
	}

	@Override
	public void attachManagers(InstanceManager<?>... listener) {
		Collections.addAll(instanceManagers, listener);
	}

	@Override
	public Vec3i getOriginCoordinate() {
		return originCoordinate;
	}

	@Override
	public void delete() {
		drawManager.delete();
	}

	@Override
	public void addDebugInfo(List<String> info) {
		info.add("GL46 Indirect");
		info.add("Origin: " + originCoordinate.getX() + ", " + originCoordinate.getY() + ", " + originCoordinate.getZ());
	}
}
