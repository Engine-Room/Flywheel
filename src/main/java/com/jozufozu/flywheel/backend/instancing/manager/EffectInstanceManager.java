package com.jozufozu.flywheel.backend.instancing.manager;

import java.util.ArrayList;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.jozufozu.flywheel.api.instance.Instance;
import com.jozufozu.flywheel.api.instance.effect.Effect;
import com.jozufozu.flywheel.api.instancer.InstancerProvider;
import com.jozufozu.flywheel.backend.instancing.storage.AbstractStorage;
import com.jozufozu.flywheel.backend.instancing.storage.Storage;

public class EffectInstanceManager extends InstanceManager<Effect> {
	private final EffectStorage<Effect> storage;

	public EffectInstanceManager(InstancerProvider instancerManager) {
		storage = new EffectStorage<>(instancerManager);
	}

	@Override
	protected Storage<Effect> getStorage() {
		return storage;
	}

	@Override
	protected boolean canCreateInstance(Effect obj) {
		return true;
	}

	private static class EffectStorage<T extends Effect> extends AbstractStorage<T> {
		private final Multimap<T, Instance> instances;

		public EffectStorage(InstancerProvider manager) {
			super(manager);
			this.instances = HashMultimap.create();
		}

		@Override
		public Iterable<Instance> getAllInstances() {
			return instances.values();
		}

		@Override
		public int getInstanceCount() {
			return instances.size();
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
			for (Instance instance : instances) {
				instance.removeNow();
			}
		}

		@Override
		public void update(T obj) {
			var instances = this.instances.get(obj);

			if (instances.isEmpty()) {
				return;
			}

			instances.forEach(Instance::update);
		}

		@Override
		public void recreateAll() {
			tickableInstances.clear();
			dynamicInstances.clear();
			instances.values().forEach(Instance::delete);

			var backup = new ArrayList<>(instances.keySet());
			instances.clear();
			backup.forEach(this::create);
		}

		@Override
		public void invalidate() {
			instances.values().forEach(Instance::delete);
			instances.clear();
			tickableInstances.clear();
			dynamicInstances.clear();
		}

		private void create(T obj) {
			var instances = obj.createInstances(instancerManager);

			this.instances.putAll(obj, instances);

			instances.forEach(this::setup);
		}
	}
}
