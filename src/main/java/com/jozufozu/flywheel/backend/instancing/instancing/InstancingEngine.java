package com.jozufozu.flywheel.backend.instancing.instancing;

import java.util.Collections;
import java.util.List;
import java.util.Set;

import org.lwjgl.opengl.GL32;

import com.jozufozu.flywheel.api.RenderStage;
import com.jozufozu.flywheel.api.instancer.InstancedPart;
import com.jozufozu.flywheel.api.instancer.Instancer;
import com.jozufozu.flywheel.api.struct.StructType;
import com.jozufozu.flywheel.backend.instancing.Engine;
import com.jozufozu.flywheel.backend.instancing.InstanceManager;
import com.jozufozu.flywheel.backend.instancing.TaskExecutor;
import com.jozufozu.flywheel.backend.instancing.compile.FlwCompiler;
import com.jozufozu.flywheel.core.ComponentRegistry;
import com.jozufozu.flywheel.core.Pipelines;
import com.jozufozu.flywheel.core.RenderContext;
import com.jozufozu.flywheel.core.context.SimpleContext;
import com.jozufozu.flywheel.core.model.Model;
import com.jozufozu.flywheel.gl.GlStateTracker;
import com.jozufozu.flywheel.gl.GlTextureUnit;
import com.jozufozu.flywheel.util.FlwUtil;
import com.mojang.blaze3d.systems.RenderSystem;

import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;

public class InstancingEngine implements Engine {

	protected final InstancingDrawManager drawManager = new InstancingDrawManager();

	/**
	 * The set of instance managers that are attached to this engine.
	 */
	private final Set<InstanceManager<?>> instanceManagers = FlwUtil.createWeakHashSet();

	protected final SimpleContext context;
	protected final int sqrMaxOriginDistance;

	protected BlockPos originCoordinate = BlockPos.ZERO;

	public InstancingEngine(SimpleContext context, int sqrMaxOriginDistance) {
		this.context = context;
		this.sqrMaxOriginDistance = sqrMaxOriginDistance;
	}

	@Override
	public <D extends InstancedPart> Instancer<D> instancer(StructType<D> type, Model model, RenderStage stage) {
		return drawManager.getInstancer(type, model, stage);
	}

	@Override
	public void beginFrame(TaskExecutor executor, RenderContext context) {
		try (var restoreState = GlStateTracker.getRestoreState()) {
			drawManager.flush();
		}
	}

	@Override
	public void renderStage(TaskExecutor executor, RenderContext context, RenderStage stage) {
		var drawSet = drawManager.get(stage);

		if (drawSet.isEmpty()) {
			return;
		}

		try (var restoreState = GlStateTracker.getRestoreState()) {
			setup();

			render(drawSet);
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
		var vertexType = desc.vertex();
		var structType = desc.instance();
		var material = desc.material();

		var program = FlwCompiler.INSTANCE.getPipelineProgram(vertexType, structType, context, Pipelines.INSTANCED_ARRAYS);
		program.bind();

		var uniformLocation = program.getUniformLocation("_flw_materialID_instancing");
		var vertexID = ComponentRegistry.materials.getVertexID(material);
		var fragmentID = ComponentRegistry.materials.getFragmentID(material);
		GL32.glUniform2ui(uniformLocation, vertexID, fragmentID);
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
		info.add("GL33 Instanced Arrays");
		info.add("Origin: " + originCoordinate.getX() + ", " + originCoordinate.getY() + ", " + originCoordinate.getZ());
	}
}
