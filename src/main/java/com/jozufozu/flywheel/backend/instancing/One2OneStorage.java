package com.jozufozu.flywheel.backend.instancing;

import java.util.HashMap;
import java.util.Map;

import org.jetbrains.annotations.Nullable;

import com.jozufozu.flywheel.api.instancer.InstancerManager;
import com.jozufozu.flywheel.light.LightUpdater;

public abstract class One2OneStorage<T> extends AbstractStorage<T> {
	private final Map<T, AbstractInstance> instances;

	public One2OneStorage(InstancerManager instancerManager) {
		super(instancerManager);
		this.instances = new HashMap<>();
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
		instances.values().forEach(AbstractInstance::remove);
		instances.clear();
		dynamicInstances.clear();
		tickableInstances.clear();
	}

	@Override
	public void add(T obj) {
		AbstractInstance instance = instances.get(obj);

		if (instance == null) {
			create(obj);
		}
	}

	@Override
	public void remove(T obj) {
		var instance = instances.remove(obj);

		if (instance == null) {
			return;
		}

		instance.remove();
		dynamicInstances.remove(instance);
		tickableInstances.remove(instance);
		LightUpdater.get(instance.level)
				.removeListener(instance);
	}

	@Override
	public void update(T obj) {
		AbstractInstance instance = instances.get(obj);

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
			instance.remove();

			AbstractInstance out = createRaw(obj);

			if (out != null) {
				setup(out);
			}

			return out;
		});
	}

	private void create(T obj) {
		AbstractInstance renderer = createRaw(obj);

		if (renderer != null) {
			setup(renderer);
			instances.put(obj, renderer);
		}

	}

	@Nullable
	protected abstract AbstractInstance createRaw(T obj);
}
