package com.jozufozu.flywheel.impl.visualization.storage;

import java.util.ArrayList;
import java.util.List;

import com.jozufozu.flywheel.api.backend.Engine;
import com.jozufozu.flywheel.api.visual.DynamicVisual;
import com.jozufozu.flywheel.api.visual.TickableVisual;
import com.jozufozu.flywheel.api.visual.Visual;

public abstract class AbstractStorage<T> implements Storage<T> {
	protected final Engine engine;
	protected final List<TickableVisual> tickableVisuals = new ArrayList<>();
	protected final List<DynamicVisual> dynamicVisuals = new ArrayList<>();

	protected AbstractStorage(Engine engine) {
		this.engine = engine;
	}

	@Override
	public List<TickableVisual> getTickableVisuals() {
		return tickableVisuals;
	}

	@Override
	public List<DynamicVisual> getDynamicVisuals() {
		return dynamicVisuals;
	}

	protected void setup(Visual visual) {
		visual.init();

		if (visual instanceof TickableVisual tickable) {
			tickableVisuals.add(tickable);
			tickable.tick();
		}

		if (visual instanceof DynamicVisual dynamic) {
			dynamicVisuals.add(dynamic);
			dynamic.beginFrame();
		}
	}
}
