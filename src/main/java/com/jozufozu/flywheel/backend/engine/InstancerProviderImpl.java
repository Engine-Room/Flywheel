package com.jozufozu.flywheel.backend.engine;

import com.jozufozu.flywheel.api.event.RenderStage;
import com.jozufozu.flywheel.api.instance.Instance;
import com.jozufozu.flywheel.api.instance.InstanceType;
import com.jozufozu.flywheel.api.instance.Instancer;
import com.jozufozu.flywheel.api.instance.InstancerProvider;
import com.jozufozu.flywheel.api.model.Model;
import com.jozufozu.flywheel.backend.engine.embed.GlobalEnvironment;

public record InstancerProviderImpl(EngineImpl engine, RenderStage renderStage) implements InstancerProvider {
	@Override
	public <I extends Instance> Instancer<I> instancer(InstanceType<I> type, Model model) {
		return engine.instancer(GlobalEnvironment.INSTANCE, type, model, renderStage);
	}
}
