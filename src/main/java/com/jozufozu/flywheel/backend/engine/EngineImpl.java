package com.jozufozu.flywheel.backend.engine;

import java.util.List;

import com.jozufozu.flywheel.api.backend.Engine;
import com.jozufozu.flywheel.api.event.RenderContext;
import com.jozufozu.flywheel.api.event.RenderStage;
import com.jozufozu.flywheel.api.instance.Instance;
import com.jozufozu.flywheel.api.instance.InstanceType;
import com.jozufozu.flywheel.api.instance.Instancer;
import com.jozufozu.flywheel.api.instance.InstancerProvider;
import com.jozufozu.flywheel.api.model.Model;
import com.jozufozu.flywheel.api.task.Plan;
import com.jozufozu.flywheel.api.task.TaskExecutor;
import com.jozufozu.flywheel.api.visualization.VisualEmbedding;
import com.jozufozu.flywheel.api.visualization.VisualizationContext;
import com.jozufozu.flywheel.backend.engine.embed.EmbeddedEnvironment;
import com.jozufozu.flywheel.backend.engine.embed.Environment;
import com.jozufozu.flywheel.backend.engine.uniform.Uniforms;
import com.jozufozu.flywheel.backend.gl.GlStateTracker;
import com.jozufozu.flywheel.lib.task.Flag;
import com.jozufozu.flywheel.lib.task.NamedFlag;
import com.jozufozu.flywheel.lib.task.SyncedPlan;

import net.minecraft.client.Camera;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.world.phys.Vec3;

public class EngineImpl implements Engine {
	private final int sqrMaxOriginDistance;
	private final DrawManager<? extends AbstractInstancer<?>> drawManager;
	private final EnvironmentStorage environmentStorage = new EnvironmentStorage(this);
	private final Flag flushFlag = new NamedFlag("flushed");

	private BlockPos renderOrigin = BlockPos.ZERO;

	public EngineImpl(DrawManager<? extends AbstractInstancer<?>> drawManager, int maxOriginDistance) {
		this.drawManager = drawManager;
		sqrMaxOriginDistance = maxOriginDistance * maxOriginDistance;
	}

	@Override
	public Plan<RenderContext> createFramePlan() {
		return SyncedPlan.of(this::flush);
	}

	@Override
	public void renderStage(TaskExecutor executor, RenderContext context, RenderStage stage) {
		executor.syncUntil(flushFlag::isRaised);
		if (stage.isLast()) {
			flushFlag.lower();
		}

		drawManager.renderStage(stage);
	}

	@Override
	public void renderCrumbling(TaskExecutor executor, RenderContext context, List<CrumblingBlock> crumblingBlocks) {
		// Need to wait for flush before we can inspect instancer state.
		executor.syncUntil(flushFlag::isRaised);

		drawManager.renderCrumbling(crumblingBlocks);
	}

	@Override
	public VisualizationContext createVisualizationContext(RenderStage stage) {
		return new VisualizationContextImpl(stage);
	}

	@Override
	public boolean updateRenderOrigin(Camera camera) {
		Vec3 cameraPos = camera.getPosition();
		double dx = renderOrigin.getX() - cameraPos.x;
		double dy = renderOrigin.getY() - cameraPos.y;
		double dz = renderOrigin.getZ() - cameraPos.z;
		double distanceSqr = dx * dx + dy * dy + dz * dz;

		if (distanceSqr <= sqrMaxOriginDistance) {
			return false;
		}

		renderOrigin = BlockPos.containing(cameraPos);
		drawManager.onRenderOriginChanged();
		return true;
	}

	@Override
	public Vec3i renderOrigin() {
		return renderOrigin;
	}

	@Override
	public void delete() {
		drawManager.delete();
		environmentStorage.delete();
	}

	public <I extends Instance> Instancer<I> instancer(Environment environment, InstanceType<I> type, Model model, RenderStage stage) {
		return drawManager.getInstancer(environment, type, model, stage);
	}

	private void flush(RenderContext ctx) {
		try (var state = GlStateTracker.getRestoreState()) {
			Uniforms.updateContext(ctx);
			drawManager.flush();
			environmentStorage.flush();
		}

		flushFlag.raise();
	}

	public VisualEmbedding createEmbedding(RenderStage renderStage) {
		return environmentStorage.create(renderStage);
	}

	public void freeEmbedding(EmbeddedEnvironment embeddedEnvironment) {
		environmentStorage.enqueueDeletion(embeddedEnvironment);
	}

	private class VisualizationContextImpl implements VisualizationContext {
		private final InstancerProviderImpl instancerProvider;
		private final RenderStage stage;

		public VisualizationContextImpl(RenderStage stage) {
			instancerProvider = new InstancerProviderImpl(EngineImpl.this, stage);
			this.stage = stage;
		}

		@Override
		public InstancerProvider instancerProvider() {
			return instancerProvider;
		}

		@Override
		public Vec3i renderOrigin() {
			return EngineImpl.this.renderOrigin();
		}

		@Override
		public VisualEmbedding createEmbedding() {
			return EngineImpl.this.createEmbedding(stage);
		}
	}
}
