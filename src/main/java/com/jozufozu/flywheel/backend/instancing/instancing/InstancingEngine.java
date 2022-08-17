package com.jozufozu.flywheel.backend.instancing.instancing;

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
import com.jozufozu.flywheel.backend.instancing.TaskEngine;
import com.jozufozu.flywheel.core.RenderContext;
import com.jozufozu.flywheel.core.compile.ContextShader;
import com.jozufozu.flywheel.core.compile.ProgramCompiler;
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

	public static int MAX_ORIGIN_DISTANCE = 100;

	protected BlockPos originCoordinate = BlockPos.ZERO;

	protected final ContextShader context;

	protected final Map<StructType<?>, GPUInstancerFactory<?>> factories = new HashMap<>();

	protected final InstancingDrawManager drawManager = new InstancingDrawManager();

	/**
	 * The set of instance managers that are attached to this engine.
	 */
	private final WeakHashSet<InstanceManager<?>> instanceManagers;

	public InstancingEngine(ContextShader context) {
		this.context = context;

		this.instanceManagers = new WeakHashSet<>();
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
	public void renderStage(TaskEngine taskEngine, RenderContext context, RenderStage stage) {
		var drawSet = drawManager.get(stage);

		if (drawSet.isEmpty()) {
			return;
		}

		setup();

		render(drawSet);
	}

	private void setup() {
		GlTextureUnit.T2.makeActive();
		Minecraft.getInstance().gameRenderer.lightTexture().turnOnLightLayer();

		RenderSystem.depthMask(true);
		RenderSystem.colorMask(true, true, true, true);
		RenderSystem.enableDepthTest();
		RenderSystem.depthFunc(GL32.GL_LEQUAL);
		RenderSystem.enableCull();
	}

	protected void render(InstancingDrawManager.DrawSet drawSet) {
		if (drawSet.isEmpty()) {
			return;
		}

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

		var ctx = new ProgramCompiler.Context(vertexType, material, instanceShader, context);

		ProgramCompiler.INSTANCE.getProgram(ctx)
				.bind();
		UniformBuffer.getInstance().sync();
	}

	@Override
	public void delete() {
		factories.clear();
		drawManager.delete();
	}

	@Override
	public Vec3i getOriginCoordinate() {
		return originCoordinate;
	}

	@Override
	public void attachManagers(InstanceManager<?>... listener) {
		instanceManagers.addAll(List.of(listener));
	}

	@Override
	public boolean maintainOriginCoordinate(Camera camera) {
		Vec3 cameraPos = camera.getPosition();

		double distanceSqr = Vec3.atLowerCornerOf(originCoordinate)
				.subtract(cameraPos)
				.lengthSqr();

		if (distanceSqr > MAX_ORIGIN_DISTANCE * MAX_ORIGIN_DISTANCE) {
			shiftListeners(Mth.floor(cameraPos.x), Mth.floor(cameraPos.y), Mth.floor(cameraPos.z));
			return true;
		}
		return false;
	}

	@Override
	public void beginFrame(TaskEngine taskEngine, RenderContext context) {
		drawManager.flush();
	}

	private void shiftListeners(int cX, int cY, int cZ) {
		originCoordinate = new BlockPos(cX, cY, cZ);

		drawManager.clearInstancers();

		instanceManagers.forEach(InstanceManager::onOriginShift);
	}

	@Override
	public void addDebugInfo(List<String> info) {
		info.add("GL33 Instanced Arrays");
		info.add("Origin: " + originCoordinate.getX() + ", " + originCoordinate.getY() + ", " + originCoordinate.getZ());
	}
}
