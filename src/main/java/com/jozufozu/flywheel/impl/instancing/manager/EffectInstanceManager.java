package com.jozufozu.flywheel.impl.instancing.manager;

import java.util.Collection;

import com.jozufozu.flywheel.api.backend.Engine;
import com.jozufozu.flywheel.api.instance.Instance;
import com.jozufozu.flywheel.api.instance.controller.InstanceContext;
import com.jozufozu.flywheel.api.instance.effect.Effect;
import com.jozufozu.flywheel.impl.instancing.storage.One2ManyStorage;
import com.jozufozu.flywheel.impl.instancing.storage.Storage;

public class EffectInstanceManager extends InstanceManager<Effect> {
	private final EffectStorage storage;

	public EffectInstanceManager(Engine engine) {
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
		protected Collection<? extends Instance> createRaw(Effect obj) {
			return obj.createInstances(new InstanceContext(engine, engine.renderOrigin()));
		}

		@Override
		public boolean willAccept(Effect obj) {
			return true;
		}
	}
}
