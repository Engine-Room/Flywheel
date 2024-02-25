package com.jozufozu.flywheel.impl.visualization;

import java.util.function.Supplier;

import com.jozufozu.flywheel.api.backend.Engine;
import com.jozufozu.flywheel.api.event.RenderStage;
import com.jozufozu.flywheel.api.instance.Instance;
import com.jozufozu.flywheel.api.instance.InstanceType;
import com.jozufozu.flywheel.api.instance.Instancer;
import com.jozufozu.flywheel.api.instance.InstancerProvider;
import com.jozufozu.flywheel.api.model.Model;
import com.jozufozu.flywheel.api.visualization.EmbeddedLevel;
import com.jozufozu.flywheel.api.visualization.VisualizationContext;

public class InstancerProviderImpl implements InstancerProvider, Supplier<VisualizationContext> {
	protected final Engine engine;
	protected final RenderStage renderStage;

	public InstancerProviderImpl(Engine engine, RenderStage renderStage) {
		this.engine = engine;
		this.renderStage = renderStage;
	}

	@Override
	public <I extends Instance> Instancer<I> instancer(InstanceType<I> type, Model model) {
		return engine.instancer(type, model, renderStage);
	}

	@Override
	public VisualizationContext get() {
		return new VisualizationContextImpl(this, engine.renderOrigin());
	}

	public Embedded embed(EmbeddedLevel world) {
		return new Embedded(engine, world, renderStage);
	}

	@Override
	public String toString() {
		return "InstancerProviderImpl[" + "engine=" + engine + ", " + "renderStage=" + renderStage + ']';
	}

	public static final class Embedded extends InstancerProviderImpl {
		private final EmbeddedLevel world;

		public Embedded(Engine engine, EmbeddedLevel world, RenderStage renderStage) {
			super(engine, renderStage);
			this.world = world;
		}

		@Override
		public <I extends Instance> Instancer<I> instancer(InstanceType<I> type, Model model) {
			return engine.instancer(world, type, model, renderStage);
		}

		public EmbeddedLevel world() {
			return world;
		}

		@Override
		public String toString() {
			return "InstancerProviderImpl.EmbeddedImpl[" + "world=" + world + ", " + "engine=" + engine + ", " + "renderStage=" + renderStage + ']';
		}
	}
}
