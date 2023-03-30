package com.jozufozu.flywheel.backend.instancing.storage;

import java.util.List;

import com.jozufozu.flywheel.api.instance.DynamicInstance;
import com.jozufozu.flywheel.api.instance.TickableInstance;
import com.jozufozu.flywheel.backend.instancing.AbstractInstance;

public interface Storage<T> {
	int getObjectCount();

	Iterable<AbstractInstance> allInstances();

	List<TickableInstance> getInstancesForTicking();

	List<DynamicInstance> getInstancesForUpdate();

	void invalidate();

	void add(T obj);

	void remove(T obj);

	void update(T obj);

	void recreateAll();
}
