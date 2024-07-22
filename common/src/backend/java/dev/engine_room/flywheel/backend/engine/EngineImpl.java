package dev.engine_room.flywheel.backend.engine;

import java.util.List;

import dev.engine_room.flywheel.api.RenderContext;
import dev.engine_room.flywheel.api.backend.Engine;
import dev.engine_room.flywheel.api.instance.Instance;
import dev.engine_room.flywheel.api.instance.InstanceType;
import dev.engine_room.flywheel.api.instance.Instancer;
import dev.engine_room.flywheel.api.instance.InstancerProvider;
import dev.engine_room.flywheel.api.model.Model;
import dev.engine_room.flywheel.api.task.Plan;
import dev.engine_room.flywheel.api.task.TaskExecutor;
import dev.engine_room.flywheel.api.visualization.VisualEmbedding;
import dev.engine_room.flywheel.api.visualization.VisualType;
import dev.engine_room.flywheel.api.visualization.VisualizationContext;
import dev.engine_room.flywheel.backend.engine.embed.EmbeddedEnvironment;
import dev.engine_room.flywheel.backend.engine.embed.Environment;
import dev.engine_room.flywheel.backend.engine.embed.EnvironmentStorage;
import dev.engine_room.flywheel.backend.engine.uniform.Uniforms;
import dev.engine_room.flywheel.backend.gl.GlStateTracker;
import dev.engine_room.flywheel.lib.task.Flag;
import dev.engine_room.flywheel.lib.task.SyncedPlan;
import it.unimi.dsi.fastutil.longs.LongSet;
import net.minecraft.client.Camera;
import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.core.Vec3i;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.phys.Vec3;

public class EngineImpl implements Engine {
	private final DrawManager<? extends AbstractInstancer<?>> drawManager;
	private final int sqrMaxOriginDistance;
	private final Flag flushFlag = new Flag("flushed");
	private final EnvironmentStorage environmentStorage;
	private final LightStorage lightStorage;

	private BlockPos renderOrigin = BlockPos.ZERO;

	public EngineImpl(LevelAccessor level, DrawManager<? extends AbstractInstancer<?>> drawManager, int maxOriginDistance) {
		this.drawManager = drawManager;
		sqrMaxOriginDistance = maxOriginDistance * maxOriginDistance;
		environmentStorage = new EnvironmentStorage();
		lightStorage = new LightStorage(level);
	}

	@Override
	public VisualizationContext createVisualizationContext(VisualType visualType) {
		return new VisualizationContextImpl(visualType);
	}

	@Override
	public Plan<RenderContext> createFramePlan() {
		return lightStorage.createFramePlan()
				.then(SyncedPlan.of(this::flush));
	}

	@Override
	public Vec3i renderOrigin() {
		return renderOrigin;
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
	public void lightSections(LongSet sections) {
		lightStorage.sections(sections);
	}

	@Override
	public void onLightUpdate(SectionPos sectionPos, LightLayer layer) {
		lightStorage.onLightUpdate(sectionPos.asLong());
	}

	@Override
	public void render(TaskExecutor executor, RenderContext context, VisualType visualType) {
		executor.syncUntil(flushFlag::isRaised);
		if (visualType == VisualType.EFFECT) {
			flushFlag.lower();
		}

		drawManager.render(visualType);
	}

	@Override
	public void renderCrumbling(TaskExecutor executor, RenderContext context, List<CrumblingBlock> crumblingBlocks) {
		// Need to wait for flush before we can inspect instancer state.
		executor.syncUntil(flushFlag::isRaised);

		drawManager.renderCrumbling(crumblingBlocks);
	}

	@Override
	public void delete() {
		drawManager.delete();
		lightStorage.delete();
	}

	public <I extends Instance> Instancer<I> instancer(Environment environment, InstanceType<I> type, Model model, VisualType visualType) {
		return drawManager.getInstancer(environment, type, model, visualType);
	}

	private void flush(RenderContext ctx) {
		try (var state = GlStateTracker.getRestoreState()) {
			Uniforms.update(ctx);
			environmentStorage.flush();
			drawManager.flush(lightStorage);
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
		private final VisualType visualType;

		public VisualizationContextImpl(VisualType visualType) {
			instancerProvider = new InstancerProviderImpl(EngineImpl.this, visualType);
			this.visualType = visualType;
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
			var out = new EmbeddedEnvironment(EngineImpl.this, visualType);
			environmentStorage.track(out);
			return out;
		}
	}
}
