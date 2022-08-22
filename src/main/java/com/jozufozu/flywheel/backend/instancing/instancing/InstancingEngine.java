package com.jozufozu.flywheel.backend.instancing.instancing;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jetbrains.annotations.NotNull;
import org.lwjgl.opengl.GL32;

import com.jozufozu.flywheel.api.RenderStage;
import com.jozufozu.flywheel.api.instancer.InstancedPart;
import com.jozufozu.flywheel.api.material.Material;
import com.jozufozu.flywheel.api.struct.StructType;
import com.jozufozu.flywheel.api.vertex.VertexType;
import com.jozufozu.flywheel.backend.gl.GlTextureUnit;
import com.jozufozu.flywheel.backend.instancing.Engine;
import com.jozufozu.flywheel.backend.instancing.InstanceManager;
import com.jozufozu.flywheel.backend.instancing.PipelineCompiler;
import com.jozufozu.flywheel.backend.instancing.TaskEngine;
import com.jozufozu.flywheel.core.Components;
import com.jozufozu.flywheel.core.RenderContext;
import com.jozufozu.flywheel.api.context.ContextShader;
import com.jozufozu.flywheel.core.source.FileResolution;
import com.jozufozu.flywheel.core.uniform.UniformBuffer;
import com.jozufozu.flywheel.util.WeakHashSet;
import com.mojang.blaze3d.systems.RenderSystem;

import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;

public class InstancingEngine implements Engine {

	protected final InstancingDrawManager drawManager = new InstancingDrawManager();
	protected final Map<StructType<?>, GPUInstancerFactory<?>> factories = new HashMap<>();

	/**
	 * The set of instance managers that are attached to this engine.
	 */
	private final WeakHashSet<InstanceManager<?>> instanceManagers = new WeakHashSet<>();

	protected final ContextShader context;
	protected final int sqrMaxOriginDistance;

	protected BlockPos originCoordinate = BlockPos.ZERO;

	public InstancingEngine(ContextShader context, int sqrMaxOriginDistance) {
		this.context = context;
		this.sqrMaxOriginDistance = sqrMaxOriginDistance;
	}

	@SuppressWarnings("unchecked")
	@NotNull
	@Override
	public <D extends InstancedPart> GPUInstancerFactory<D> factory(StructType<D> type) {
		return (GPUInstancerFactory<D>) factories.computeIfAbsent(type, this::createFactory);
	}

	@NotNull
	private <D extends InstancedPart> GPUInstancerFactory<D> createFactory(StructType<D> type) {
		return new GPUInstancerFactory<>(type, drawManager::create);
	}

	@Override
	public void beginFrame(TaskEngine taskEngine, RenderContext context) {
		drawManager.flush();
	}

	@Override
	public void renderStage(TaskEngine taskEngine, RenderContext context, RenderStage stage) {
		var drawSet = drawManager.get(stage);

		if (drawSet.isEmpty()) {
			return;
		}

		setup();

		render(drawSet);
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

	protected void render(InstancingDrawManager.DrawSet drawSet) {
		for (var entry : drawSet) {
			var shader = entry.getKey();
			var drawCalls = entry.getValue();

			drawCalls.removeIf(DrawCall::shouldRemove);

			if (drawCalls.isEmpty()) {
				continue;
			}

			setup(shader);

			shader.material().setup();

			for (var drawCall : drawCalls) {
				drawCall.render();
			}

			shader.material().clear();
		}
	}

	protected void setup(ShaderState desc) {
		VertexType vertexType = desc.vertex();
		FileResolution instanceShader = desc.instance()
				.getInstanceShader();
		Material material = desc.material();

		var ctx = new PipelineCompiler.Context(vertexType, material, instanceShader, context, Components.INSTANCED_ARRAYS);

		PipelineCompiler.INSTANCE.getProgram(ctx)
				.bind();
		UniformBuffer.getInstance().sync();
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
		factories.clear();
		drawManager.delete();
	}

	@Override
	public void addDebugInfo(List<String> info) {
		info.add("GL33 Instanced Arrays");
		info.add("Origin: " + originCoordinate.getX() + ", " + originCoordinate.getY() + ", " + originCoordinate.getZ());
	}
}
