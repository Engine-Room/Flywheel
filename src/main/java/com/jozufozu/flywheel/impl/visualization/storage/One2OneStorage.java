package com.jozufozu.flywheel.impl.visualization.storage;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.jetbrains.annotations.Nullable;

import com.jozufozu.flywheel.api.backend.Engine;
import com.jozufozu.flywheel.api.visual.Visual;

public abstract class One2OneStorage<T> extends AbstractStorage<T> {
	private final Map<T, Visual> visuals = new HashMap<>();

	public One2OneStorage(Engine engine) {
		super(engine);
	}

	@Override
	public Collection<Visual> getAllVisuals() {
		return visuals.values();
	}

	@Override
	public void add(T obj) {
		Visual visual = visuals.get(obj);

		if (visual == null) {
			create(obj);
		}
	}

	@Override
	public void remove(T obj) {
		Visual visual = visuals.remove(obj);

		if (visual == null) {
			return;
		}

		tickableVisuals.remove(visual);
		dynamicVisuals.remove(visual);
		visual.delete();
	}

	@Override
	public void update(T obj) {
		Visual visual = visuals.get(obj);

		if (visual == null) {
			return;
		}

		// resetting visuals is by default used to handle block state changes.
		if (visual.shouldReset()) {
			// delete and re-create the visual.
			// resetting a visual supersedes updating it.
			remove(obj);
			create(obj);
		} else {
			visual.update();
		}
	}

	@Override
	public void recreateAll() {
		tickableVisuals.clear();
		dynamicVisuals.clear();
		visuals.replaceAll((obj, visual) -> {
			visual.delete();

			Visual out = createRaw(obj);

			if (out != null) {
				setup(out);
			}

			return out;
		});
	}

	@Override
	public void invalidate() {
		tickableVisuals.clear();
		dynamicVisuals.clear();
		visuals.values().forEach(Visual::delete);
		visuals.clear();
	}

	private void create(T obj) {
		Visual visual = createRaw(obj);

		if (visual != null) {
			setup(visual);
			visuals.put(obj, visual);
		}
	}

	@Nullable
	protected abstract Visual createRaw(T obj);
}
