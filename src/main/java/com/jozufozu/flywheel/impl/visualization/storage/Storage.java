package com.jozufozu.flywheel.impl.visualization.storage;

import java.util.Collection;
import java.util.List;

import com.jozufozu.flywheel.api.visual.DynamicVisual;
import com.jozufozu.flywheel.api.visual.TickableVisual;
import com.jozufozu.flywheel.api.visual.Visual;

public interface Storage<T> {
	Collection<Visual> getAllVisuals();

	List<TickableVisual> getTickableVisuals();

	List<DynamicVisual> getDynamicVisuals();

	/**
	 * Is the given object currently capable of being added?
	 *
	 * @return true if the object is currently capable of being visualized.
	 */
	boolean willAccept(T obj);

	void add(T obj);

	void remove(T obj);

	void update(T obj);

	void recreateAll();

	void invalidate();
}
