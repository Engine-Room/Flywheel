package dev.engine_room.flywheel.backend.engine;

import java.util.List;

import dev.engine_room.flywheel.api.backend.Engine;
import dev.engine_room.flywheel.api.event.RenderContext;
import dev.engine_room.flywheel.api.event.RenderStage;
import dev.engine_room.flywheel.api.instance.Instance;
import dev.engine_room.flywheel.api.instance.InstanceType;
import dev.engine_room.flywheel.api.instance.Instancer;
import dev.engine_room.flywheel.api.instance.InstancerProvider;
import dev.engine_room.flywheel.api.model.Model;
import dev.engine_room.flywheel.api.task.Plan;
import dev.engine_room.flywheel.api.task.TaskExecutor;
import dev.engine_room.flywheel.api.visualization.VisualEmbedding;
import dev.engine_room.flywheel.api.visualization.VisualizationContext;
import dev.engine_room.flywheel.backend.engine.embed.Environment;
import dev.engine_room.flywheel.backend.engine.embed.LightStorage;
import dev.engine_room.flywheel.backend.engine.embed.TopLevelEmbeddedEnvironment;
import dev.engine_room.flywheel.backend.engine.uniform.Uniforms;
import dev.engine_room.flywheel.backend.gl.GlStateTracker;
import dev.engine_room.flywheel.lib.task.Flag;
import dev.engine_room.flywheel.lib.task.NamedFlag;
import dev.engine_room.flywheel.lib.task.SyncedPlan;
import net.minecraft.client.Camera;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.phys.Vec3;

public class EngineImpl implements Engine {
	private final int sqrMaxOriginDistance;
	private final DrawManager<? extends AbstractInstancer<?>> drawManager;
	private final EnvironmentStorage environmentStorage = new EnvironmentStorage();
	private final LightStorage lightStorage;
	private final Flag flushFlag = new NamedFlag("flushed");

	private BlockPos renderOrigin = BlockPos.ZERO;

	public EngineImpl(LevelAccessor level, DrawManager<? extends AbstractInstancer<?>> drawManager, int maxOriginDistance) {
		this.drawManager = drawManager;
		sqrMaxOriginDistance = maxOriginDistance * maxOriginDistance;
		lightStorage = new LightStorage(level);
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
		lightStorage.delete();
	}

	public <I extends Instance> Instancer<I> instancer(Environment environment, InstanceType<I> type, Model model, RenderStage stage) {
		return drawManager.getInstancer(environment, type, model, stage);
	}

	private void flush(RenderContext ctx) {
		try (var state = GlStateTracker.getRestoreState()) {
			Uniforms.update(ctx);
			drawManager.flush();
			environmentStorage.flush();
		}

		flushFlag.raise();
	}

	public EnvironmentStorage environmentStorage() {
		return environmentStorage;
	}

	public LightStorage lightStorage() {
		return lightStorage;
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
			var out = new TopLevelEmbeddedEnvironment(EngineImpl.this, stage);
			environmentStorage.track(out);
			return out;
		}
	}
}
