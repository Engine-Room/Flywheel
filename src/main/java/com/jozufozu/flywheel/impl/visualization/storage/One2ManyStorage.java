package com.jozufozu.flywheel.impl.visualization.storage;

import java.util.Collection;
import java.util.List;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.jozufozu.flywheel.api.backend.Engine;
import com.jozufozu.flywheel.api.visual.Visual;

public abstract class One2ManyStorage<T> extends AbstractStorage<T> {
	private final Multimap<T, Visual> allVisuals = HashMultimap.create();

	public One2ManyStorage(Engine engine) {
		super(engine);
	}

	@Override
	public Collection<Visual> getAllVisuals() {
		return allVisuals.values();
	}

	@Override
	public void add(T obj) {
		Collection<Visual> visuals = allVisuals.get(obj);

		if (visuals.isEmpty()) {
			create(obj);
		}
	}

	@Override
	public void remove(T obj) {
		Collection<Visual> visuals = allVisuals.removeAll(obj);

		if (visuals.isEmpty()) {
			return;
		}

		tickableVisuals.removeAll(visuals);
		dynamicVisuals.removeAll(visuals);
		visuals.forEach(Visual::delete);
	}

	@Override
	public void update(T obj) {
		Collection<Visual> visuals = allVisuals.get(obj);

		if (visuals.isEmpty()) {
			return;
		}

		// TODO: shouldReset cannot be checked here because all visuals are created at once
		visuals.forEach(Visual::update);
	}

	@Override
	public void recreateAll() {
		tickableVisuals.clear();
		dynamicVisuals.clear();
		allVisuals.values().forEach(Visual::delete);

		List<T> objects = List.copyOf(allVisuals.keySet());
		allVisuals.clear();
		objects.forEach(this::create);
	}

	@Override
	public void invalidate() {
		tickableVisuals.clear();
		dynamicVisuals.clear();
		allVisuals.values().forEach(Visual::delete);
		allVisuals.clear();
	}

	private void create(T obj) {
		Collection<? extends Visual> visuals = createRaw(obj);

		if (!visuals.isEmpty()) {
			visuals.forEach(this::setup);
			allVisuals.putAll(obj, visuals);
		}
	}

	protected abstract Collection<? extends Visual> createRaw(T obj);
}
