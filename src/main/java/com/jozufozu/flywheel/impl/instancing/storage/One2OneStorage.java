package com.jozufozu.flywheel.impl.instancing.storage;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.jetbrains.annotations.Nullable;

import com.jozufozu.flywheel.api.backend.Engine;
import com.jozufozu.flywheel.api.instance.Instance;

public abstract class One2OneStorage<T> extends AbstractStorage<T> {
	private final Map<T, Instance> instances = new HashMap<>();

	public One2OneStorage(Engine engine) {
		super(engine);
	}

	@Override
	public Collection<Instance> getAllInstances() {
		return instances.values();
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

		tickableInstances.remove(instance);
		dynamicInstances.remove(instance);
		instance.delete();
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
		tickableInstances.clear();
		dynamicInstances.clear();
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
		tickableInstances.clear();
		dynamicInstances.clear();
		instances.values().forEach(Instance::delete);
		instances.clear();
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
