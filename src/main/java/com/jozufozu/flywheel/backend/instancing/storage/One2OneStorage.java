package com.jozufozu.flywheel.backend.instancing.storage;

import java.util.HashMap;
import java.util.Map;

import org.jetbrains.annotations.Nullable;

import com.jozufozu.flywheel.api.instance.Instance;
import com.jozufozu.flywheel.api.instancer.InstancerProvider;

public abstract class One2OneStorage<T> extends AbstractStorage<T> {
	private final Map<T, Instance> instances;

	public One2OneStorage(InstancerProvider instancerManager) {
		super(instancerManager);
		this.instances = new HashMap<>();
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
		Instance instance = instances.get(obj);

		if (instance == null) {
			create(obj);
		}
	}

	@Override
	public void remove(T obj) {
		Instance instance = instances.remove(obj);

		if (instance == null) {
			return;
		}

		instance.delete();
		dynamicInstances.remove(instance);
		tickableInstances.remove(instance);
		instance.removeNow();
	}

	@Override
	public void update(T obj) {
		Instance instance = instances.get(obj);

		if (instance == null) {
			return;
		}

		// resetting instances is by default used to handle block state changes.
		if (instance.shouldReset()) {
			// delete and re-create the instance.
			// resetting an instance supersedes updating it.
			remove(obj);
			create(obj);
		} else {
			instance.update();
		}
	}

	@Override
	public void recreateAll() {
		dynamicInstances.clear();
		tickableInstances.clear();
		instances.replaceAll((obj, instance) -> {
			instance.delete();

			Instance out = createRaw(obj);

			if (out != null) {
				setup(out);
			}

			return out;
		});
	}

	@Override
	public void invalidate() {
		instances.values().forEach(Instance::delete);
		instances.clear();
		tickableInstances.clear();
		dynamicInstances.clear();
	}

	private void create(T obj) {
		Instance instance = createRaw(obj);

		if (instance != null) {
			setup(instance);
			instances.put(obj, instance);
		}
	}

	@Nullable
	protected abstract Instance createRaw(T obj);
}
