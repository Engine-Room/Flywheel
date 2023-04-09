package com.jozufozu.flywheel.impl.visualization.manager;

import java.util.Collection;

import com.jozufozu.flywheel.api.backend.Engine;
import com.jozufozu.flywheel.api.visual.Effect;
import com.jozufozu.flywheel.api.visual.Visual;
import com.jozufozu.flywheel.api.visualization.VisualizationContext;
import com.jozufozu.flywheel.impl.visualization.storage.One2ManyStorage;
import com.jozufozu.flywheel.impl.visualization.storage.Storage;

public class EffectVisualManager extends VisualManager<Effect> {
	private final EffectStorage storage;

	public EffectVisualManager(Engine engine) {
		storage = new EffectStorage(engine);
	}

	@Override
	protected Storage<Effect> getStorage() {
		return storage;
	}

	private static class EffectStorage extends One2ManyStorage<Effect> {
		public EffectStorage(Engine engine) {
			super(engine);
		}

		@Override
		protected Collection<? extends Visual> createRaw(Effect obj) {
			return obj.createVisuals(new VisualizationContext(engine, engine.renderOrigin()));
		}

		@Override
		public boolean willAccept(Effect obj) {
			return true;
		}
	}
}
