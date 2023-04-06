package com.jozufozu.flywheel.impl.instancing.storage;

import java.util.Collection;
import java.util.List;

import com.jozufozu.flywheel.api.instance.DynamicInstance;
import com.jozufozu.flywheel.api.instance.Instance;
import com.jozufozu.flywheel.api.instance.TickableInstance;

public interface Storage<T> {
	Collection<Instance> getAllInstances();

	List<TickableInstance> getTickableInstances();

	List<DynamicInstance> getDynamicInstances();

	void add(T obj);

	void remove(T obj);

	void update(T obj);

	void recreateAll();

	void invalidate();
}
