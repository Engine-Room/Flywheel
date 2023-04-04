package com.jozufozu.flywheel.backend.instancing.storage;

import java.util.ArrayList;
import java.util.List;

import com.jozufozu.flywheel.api.instance.DynamicInstance;
import com.jozufozu.flywheel.api.instance.Instance;
import com.jozufozu.flywheel.api.instance.TickableInstance;
import com.jozufozu.flywheel.api.instancer.InstancerProvider;

public abstract class AbstractStorage<T> implements Storage<T> {
	protected final InstancerProvider instancerManager;
	protected final List<TickableInstance> tickableInstances = new ArrayList<>();
	protected final List<DynamicInstance> dynamicInstances = new ArrayList<>();

	protected AbstractStorage(InstancerProvider instancerManager) {
		this.instancerManager = instancerManager;
	}

	@Override
	public List<TickableInstance> getInstancesForTicking() {
		return tickableInstances;
	}

	@Override
	public List<DynamicInstance> getInstancesForUpdate() {
		return dynamicInstances;
	}

	protected void setup(Instance instance) {
		instance.init();

		if (instance instanceof TickableInstance tickable) {
			tickableInstances.add(tickable);
			tickable.tick();
		}

		if (instance instanceof DynamicInstance dynamic) {
			dynamicInstances.add(dynamic);
			dynamic.beginFrame();
		}
	}
}
