package com.jozufozu.flywheel.impl.instancing.storage;

import java.util.Collection;
import java.util.List;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.jozufozu.flywheel.api.backend.Engine;
import com.jozufozu.flywheel.api.instance.Instance;

public abstract class One2ManyStorage<T> extends AbstractStorage<T> {
	private final Multimap<T, Instance> allInstances = HashMultimap.create();

	public One2ManyStorage(Engine engine) {
		super(engine);
	}

	@Override
	public Collection<Instance> getAllInstances() {
		return allInstances.values();
	}

	@Override
	public void add(T obj) {
		Collection<Instance> instances = allInstances.get(obj);

		if (instances.isEmpty()) {
			create(obj);
		}
	}

	@Override
	public void remove(T obj) {
		Collection<Instance> instances = allInstances.removeAll(obj);

		if (instances.isEmpty()) {
			return;
		}

		tickableInstances.removeAll(instances);
		dynamicInstances.removeAll(instances);
		instances.forEach(Instance::delete);
	}

	@Override
	public void update(T obj) {
		Collection<Instance> instances = allInstances.get(obj);

		if (instances.isEmpty()) {
			return;
		}

		// TODO: shouldReset cannot be checked here because all instances are created at once
		instances.forEach(Instance::update);
	}

	@Override
	public void recreateAll() {
		tickableInstances.clear();
		dynamicInstances.clear();
		allInstances.values().forEach(Instance::delete);

		List<T> objects = List.copyOf(allInstances.keySet());
		allInstances.clear();
		objects.forEach(this::create);
	}

	@Override
	public void invalidate() {
		tickableInstances.clear();
		dynamicInstances.clear();
		allInstances.values().forEach(Instance::delete);
		allInstances.clear();
	}

	private void create(T obj) {
		Collection<? extends Instance> instances = createRaw(obj);

		if (!instances.isEmpty()) {
			instances.forEach(this::setup);
			allInstances.putAll(obj, instances);
		}
	}

	protected abstract Collection<? extends Instance> createRaw(T obj);
}
