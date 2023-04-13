package com.jozufozu.flywheel.impl.instancing.storage;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.jozufozu.flywheel.api.backend.Engine;
import com.jozufozu.flywheel.api.instance.DynamicInstance;
import com.jozufozu.flywheel.api.instance.Instance;
import com.jozufozu.flywheel.api.instance.TickableInstance;

public abstract class AbstractStorage<T> implements Storage<T> {
	protected final Engine engine;
	protected final List<TickableInstance> tickableInstances = new ArrayList<>();
	protected final List<TickableInstance> tickableInstancesView = Collections.unmodifiableList(tickableInstances);
	protected final List<DynamicInstance> dynamicInstances = new ArrayList<>();
	protected final List<DynamicInstance> dynamicInstancesView = Collections.unmodifiableList(dynamicInstances);

	protected AbstractStorage(Engine engine) {
		this.engine = engine;
	}

	@Override
	public List<TickableInstance> getTickableInstances() {
		return tickableInstancesView;
	}

	@Override
	public List<DynamicInstance> getDynamicInstances() {
		return dynamicInstancesView;
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
