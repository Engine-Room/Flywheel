package dev.engine_room.flywheel.impl.visualization.storage;

import java.util.ArrayList;
import java.util.List;

import dev.engine_room.flywheel.api.visual.SectionTrackedVisual;
import it.unimi.dsi.fastutil.longs.LongArraySet;
import it.unimi.dsi.fastutil.longs.LongSet;

public class SectionCollectorImpl implements SectionTrackedVisual.SectionCollector {
	public final LongSet sections = new LongArraySet();

	private final List<Runnable> listeners = new ArrayList<>(2);

	@Override
	public void sections(LongSet sections) {
		this.sections.clear();
		this.sections.addAll(sections);

		listeners.forEach(Runnable::run);
	}

	public void addListener(Runnable listener) {
		listeners.add(listener);
	}
}
