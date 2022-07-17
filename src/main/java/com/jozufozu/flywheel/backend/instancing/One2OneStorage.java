package com.jozufozu.flywheel.backend.instancing;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import org.jetbrains.annotations.Nullable;

import com.jozufozu.flywheel.api.InstancerManager;
import com.jozufozu.flywheel.api.instance.DynamicInstance;
import com.jozufozu.flywheel.api.instance.TickableInstance;
import com.jozufozu.flywheel.light.LightUpdater;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;

public abstract class One2OneStorage<T> implements Storage<T> {
	private final Map<T, AbstractInstance> instances;
	private final Object2ObjectOpenHashMap<T, TickableInstance> tickableInstances;
	private final Object2ObjectOpenHashMap<T, DynamicInstance> dynamicInstances;
	protected final InstancerManager instancerManager;

	public One2OneStorage(InstancerManager instancerManager) {
		this.instancerManager = instancerManager;
		this.instances = new HashMap<>();

		this.dynamicInstances = new Object2ObjectOpenHashMap<>();
		this.tickableInstances = new Object2ObjectOpenHashMap<>();
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
		return new ArrayList<>(tickableInstances.values());
	}

	@Override
	public List<DynamicInstance> getInstancesForUpdate() {
		return new ArrayList<>(dynamicInstances.values());
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
		dynamicInstances.remove(obj);
		tickableInstances.remove(obj);
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
				setup(obj, out);
			}

			return out;
		});
	}

	private void create(T obj) {
		AbstractInstance renderer = createRaw(obj);

		if (renderer != null) {
			setup(obj, renderer);
			instances.put(obj, renderer);
		}

	}

	@Nullable
	protected abstract AbstractInstance createRaw(T obj);

	private void setup(T obj, AbstractInstance renderer) {
		renderer.init();
		renderer.updateLight();
		LightUpdater.get(renderer.level)
				.addListener(renderer);
		if (renderer instanceof TickableInstance r) {
			tickableInstances.put(obj, r);
			r.tick();
		}

		if (renderer instanceof DynamicInstance r) {
			dynamicInstances.put(obj, r);
			r.beginFrame();
		}
	}
}
