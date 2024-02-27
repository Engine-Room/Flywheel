package com.jozufozu.flywheel.backend.engine;

import com.jozufozu.flywheel.api.backend.Engine;
import com.jozufozu.flywheel.api.event.RenderStage;
import com.jozufozu.flywheel.api.instance.Instance;
import com.jozufozu.flywheel.api.instance.InstanceType;
import com.jozufozu.flywheel.api.instance.Instancer;
import com.jozufozu.flywheel.api.instance.InstancerProvider;
import com.jozufozu.flywheel.api.model.Model;
import com.jozufozu.flywheel.api.visualization.VisualEmbedding;
import com.jozufozu.flywheel.api.visualization.VisualizationContext;

import net.minecraft.client.Camera;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.world.phys.Vec3;

public abstract class AbstractEngine implements Engine {
	protected final int sqrMaxOriginDistance;
	protected BlockPos renderOrigin = BlockPos.ZERO;

	public AbstractEngine(int maxOriginDistance) {
		sqrMaxOriginDistance = maxOriginDistance * maxOriginDistance;
	}

	public <I extends Instance> Instancer<I> instancer(Environment environment, InstanceType<I> type, Model model, RenderStage stage) {
		return getStorage().getInstancer(environment, type, model, stage);
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
		getStorage().onRenderOriginChanged();
		return true;
	}

	@Override
	public Vec3i renderOrigin() {
		return renderOrigin;
	}

	protected abstract InstancerStorage<? extends AbstractInstancer<?>> getStorage();

	private class VisualizationContextImpl implements VisualizationContext {
		private final InstancerProviderImpl instancerProvider;
		private final RenderStage stage;

		public VisualizationContextImpl(RenderStage stage) {
			instancerProvider = new InstancerProviderImpl(AbstractEngine.this, stage);
			this.stage = stage;
		}

		@Override
		public InstancerProvider instancerProvider() {
			return instancerProvider;
		}

		@Override
		public Vec3i renderOrigin() {
			return AbstractEngine.this.renderOrigin();
		}

		@Override
		public VisualEmbedding createEmbedding() {
			return new EmbeddedEnvironment(AbstractEngine.this, stage);
		}
	}
}
