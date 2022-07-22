package com.jozufozu.flywheel.backend.instancing.effect;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.jozufozu.flywheel.api.instance.DynamicInstance;
import com.jozufozu.flywheel.api.instance.TickableInstance;
import com.jozufozu.flywheel.api.instancer.InstancerManager;
import com.jozufozu.flywheel.backend.instancing.AbstractInstance;
import com.jozufozu.flywheel.backend.instancing.InstanceManager;
import com.jozufozu.flywheel.backend.instancing.Storage;
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

	public static class EffectStorage<T extends Effect> implements Storage<T> {

		private final Multimap<T, AbstractInstance> instances;
		private final Set<DynamicInstance> dynamicInstances;
		private final Set<TickableInstance> tickableInstances;
		private final InstancerManager manager;

		public EffectStorage(InstancerManager manager) {
			this.instances = HashMultimap.create();
			this.dynamicInstances = new HashSet<>();
			this.tickableInstances = new HashSet<>();
			this.manager = manager;
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
		public List<TickableInstance> getInstancesForTicking() {
			return new ArrayList<>(tickableInstances);
		}

		@Override
		public List<DynamicInstance> getInstancesForUpdate() {
			return new ArrayList<>(dynamicInstances);
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
			var instances = obj.createInstances(manager);

			this.instances.putAll(obj, instances);

			instances.forEach(this::setup);
		}

		private void setup(AbstractInstance renderer) {
			renderer.init();
			renderer.updateLight();
			LightUpdater.get(renderer.level)
					.addListener(renderer);
			if (renderer instanceof TickableInstance r) {
				tickableInstances.add(r);
				r.tick();
			}

			if (renderer instanceof DynamicInstance r) {
				dynamicInstances.add(r);
				r.beginFrame();
			}
		}
	}
}
