package com.jozufozu.flywheel.backend.instancing.effect;

import java.util.ArrayList;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.jozufozu.flywheel.api.instancer.InstancerManager;
import com.jozufozu.flywheel.backend.instancing.AbstractInstance;
import com.jozufozu.flywheel.backend.instancing.InstanceManager;
import com.jozufozu.flywheel.backend.instancing.storage.AbstractStorage;
import com.jozufozu.flywheel.backend.instancing.storage.Storage;
import com.jozufozu.flywheel.light.LightUpdater;

public class EffectInstanceManager extends InstanceManager<Effect> {

	private final EffectStorage<Effect> storage;

	public EffectInstanceManager(InstancerManager instancerManager) {
		storage = new EffectStorage<>(instancerManager);
	}

	@Override
	public Storage<Effect> getStorage() {
		return storage;
	}

	@Override
	protected boolean canCreateInstance(Effect obj) {
		return true;
	}

	public static class EffectStorage<T extends Effect> extends AbstractStorage<T> {

		private final Multimap<T, AbstractInstance> instances;

		public EffectStorage(InstancerManager manager) {
			super(manager);
			this.instances = HashMultimap.create();
		}

		@Override
		public int getObjectCount() {
			return instances.size();
		}

		@Override
		public Iterable<AbstractInstance> allInstances() {
			return instances.values();
		}

		@Override
		public void invalidate() {
			instances.values().forEach(AbstractInstance::removeAndMark);
			instances.clear();
			tickableInstances.clear();
			dynamicInstances.clear();
		}

		@Override
		public void add(T obj) {
			var instances = this.instances.get(obj);

			if (instances.isEmpty()) {
				create(obj);
			}
		}

		@Override
		public void remove(T obj) {
			var instances = this.instances.removeAll(obj);

			if (instances.isEmpty()) {
				return;
			}

			this.tickableInstances.removeAll(instances);
			this.dynamicInstances.removeAll(instances);
			for (AbstractInstance instance : instances) {
				LightUpdater.get(instance.level)
						.removeListener(instance);
			}
		}

		@Override
		public void update(T obj) {
			var instances = this.instances.get(obj);

			if (instances.isEmpty()) {
				return;
			}

			instances.forEach(AbstractInstance::update);
		}

		@Override
		public void recreateAll() {
			this.dynamicInstances.clear();
			this.tickableInstances.clear();
			this.instances.values().forEach(AbstractInstance::removeAndMark);

			var backup = new ArrayList<>(this.instances.keySet());
			this.instances.clear();
			backup.forEach(this::create);
		}

		private void create(T obj) {
			var instances = obj.createInstances(instancerManager);

			this.instances.putAll(obj, instances);

			instances.forEach(this::setup);
		}
	}
}
