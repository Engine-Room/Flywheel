package dev.engine_room.flywheel.backend.engine;

import java.util.List;

import com.mojang.blaze3d.systems.RenderSystem;

import dev.engine_room.flywheel.api.RenderContext;
import dev.engine_room.flywheel.api.backend.Engine;
import dev.engine_room.flywheel.api.instance.Instance;
import dev.engine_room.flywheel.api.instance.InstanceType;
import dev.engine_room.flywheel.api.instance.Instancer;
import dev.engine_room.flywheel.api.instance.InstancerProvider;
import dev.engine_room.flywheel.api.model.Model;
import dev.engine_room.flywheel.api.task.Plan;
import dev.engine_room.flywheel.api.visualization.VisualEmbedding;
import dev.engine_room.flywheel.api.visualization.VisualType;
import dev.engine_room.flywheel.api.visualization.VisualizationContext;
import dev.engine_room.flywheel.backend.FlwBackend;
import dev.engine_room.flywheel.backend.compile.core.ShaderException;
import dev.engine_room.flywheel.backend.engine.embed.EmbeddedEnvironment;
import dev.engine_room.flywheel.backend.engine.embed.Environment;
import dev.engine_room.flywheel.backend.engine.embed.EnvironmentStorage;
import dev.engine_room.flywheel.backend.engine.uniform.Uniforms;
import dev.engine_room.flywheel.backend.gl.GlStateTracker;
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
		return drawManager.createFramePlan()
				.and(lightStorage.createFramePlan());
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
	public void setupRender(RenderContext context) {
		try (var state = GlStateTracker.getRestoreState()) {
			RenderSystem.replayQueue();
			Uniforms.update(context);
			environmentStorage.flush();
			drawManager.flush(lightStorage, environmentStorage);
		} catch (ShaderException e) {
			FlwBackend.LOGGER.error("Falling back", e);
			triggerFallback();
		}
	}

	@Override
	public void render(RenderContext context, VisualType visualType) {
		try (var state = GlStateTracker.getRestoreState()) {
			drawManager.render(visualType);
		} catch (ShaderException e) {
			FlwBackend.LOGGER.error("Falling back", e);
			triggerFallback();
		}
	}

	@Override
	public void renderCrumbling(RenderContext context, List<CrumblingBlock> crumblingBlocks) {
		try (var state = GlStateTracker.getRestoreState()) {
			drawManager.renderCrumbling(crumblingBlocks);
		} catch (ShaderException e) {
			FlwBackend.LOGGER.error("Falling back", e);
			triggerFallback();
		}
	}

	@Override
	public void delete() {
		drawManager.delete();
		lightStorage.delete();
		environmentStorage.delete();
	}

	private void triggerFallback() {
		drawManager.triggerFallback();
	}

	public <I extends Instance> Instancer<I> instancer(Environment environment, InstanceType<I> type, Model model, VisualType visualType, int bias) {
		return drawManager.getInstancer(environment, type, model, visualType, bias);
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
		public VisualEmbedding createEmbedding(Vec3i renderOrigin) {
			var out = new EmbeddedEnvironment(EngineImpl.this, visualType, renderOrigin);
			environmentStorage.track(out);
			return out;
		}
	}
}
