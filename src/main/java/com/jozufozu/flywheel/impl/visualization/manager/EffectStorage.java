package com.jozufozu.flywheel.impl.visualization.manager;

import com.jozufozu.flywheel.api.backend.Engine;
import com.jozufozu.flywheel.api.visual.Effect;
import com.jozufozu.flywheel.api.visual.EffectVisual;
import com.jozufozu.flywheel.api.visualization.VisualizationContext;
import com.jozufozu.flywheel.impl.visualization.storage.Storage;

public class EffectStorage extends Storage<Effect> {
	public EffectStorage(Engine engine) {
		super(engine);
	}

	@Override
	protected EffectVisual<?> createRaw(Effect obj) {
		return obj.visualize(new VisualizationContext(engine, engine.renderOrigin()));
	}

	@Override
	public boolean willAccept(Effect obj) {
		return true;
	}
}
