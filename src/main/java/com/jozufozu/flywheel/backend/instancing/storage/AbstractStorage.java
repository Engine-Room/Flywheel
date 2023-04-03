package com.jozufozu.flywheel.backend.instancing.storage;

import java.util.ArrayList;
import java.util.List;

import com.jozufozu.flywheel.api.instance.DynamicInstance;
import com.jozufozu.flywheel.api.instance.TickableInstance;
import com.jozufozu.flywheel.api.instancer.InstancerProvider;
import com.jozufozu.flywheel.backend.instancing.AbstractInstance;
import com.jozufozu.flywheel.lib.light.LightUpdater;

public abstract class AbstractStorage<T> implements Storage<T> {
	protected final List<TickableInstance> tickableInstances;
	protected final List<DynamicInstance> dynamicInstances;
	protected final InstancerProvider instancerManager;

	protected AbstractStorage(InstancerProvider instancerManager) {
		this.instancerManager = instancerManager;

		this.dynamicInstances = new ArrayList<>();
		this.tickableInstances = new ArrayList<>();
	}

	@Override
	public List<TickableInstance> getInstancesForTicking() {
		return tickableInstances;
	}

	@Override
	public List<DynamicInstance> getInstancesForUpdate() {
		return dynamicInstances;
	}

	protected void setup(AbstractInstance renderer) {
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
