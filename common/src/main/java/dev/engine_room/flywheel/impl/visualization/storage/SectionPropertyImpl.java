package dev.engine_room.flywheel.impl.visualization.storage;

import java.util.ArrayList;
import java.util.List;

import dev.engine_room.flywheel.api.visual.SmoothLitVisual;
import it.unimi.dsi.fastutil.longs.LongArraySet;
import it.unimi.dsi.fastutil.longs.LongSet;

public class SectionPropertyImpl implements SmoothLitVisual.SectionProperty {
	public final LongSet sections = new LongArraySet();

	private final List<Runnable> listeners = new ArrayList<>(2);

	@Override
	public void lightSections(LongSet sections) {
		this.sections.clear();
		this.sections.addAll(sections);

		listeners.forEach(Runnable::run);
	}

	public void addListener(Runnable listener) {
		listeners.add(listener);
	}
}
