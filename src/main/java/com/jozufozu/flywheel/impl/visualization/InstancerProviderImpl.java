package com.jozufozu.flywheel.impl.visualization;

import java.util.function.Supplier;

import com.jozufozu.flywheel.api.backend.Engine;
import com.jozufozu.flywheel.api.context.Context;
import com.jozufozu.flywheel.api.event.RenderStage;
import com.jozufozu.flywheel.api.instance.Instance;
import com.jozufozu.flywheel.api.instance.InstanceType;
import com.jozufozu.flywheel.api.instance.Instancer;
import com.jozufozu.flywheel.api.instance.InstancerProvider;
import com.jozufozu.flywheel.api.model.Model;
import com.jozufozu.flywheel.api.visualization.VisualizationContext;

public record InstancerProviderImpl(Engine engine,
									Context context,
									RenderStage renderStage) implements InstancerProvider, Supplier<VisualizationContext> {
	@Override
	public <I extends Instance> Instancer<I> instancer(InstanceType<I> type, Model model) {
		return engine.instancer(type, context, model, renderStage);
	}

	@Override
	public VisualizationContext get() {
		return new VisualizationContextImpl(this, engine.renderOrigin());
	}
}
