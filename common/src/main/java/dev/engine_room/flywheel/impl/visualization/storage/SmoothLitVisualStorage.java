package dev.engine_room.flywheel.impl.visualization.storage;

import java.util.Map;

import org.jetbrains.annotations.Nullable;

import dev.engine_room.flywheel.api.visual.SmoothLitVisual;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import it.unimi.dsi.fastutil.longs.LongSet;
import it.unimi.dsi.fastutil.objects.Reference2ObjectOpenHashMap;

public class SmoothLitVisualStorage {
	private final Map<SmoothLitVisual, SectionPropertyImpl> visuals = new Reference2ObjectOpenHashMap<>();

	@Nullable
	private LongSet cachedSections;

	public boolean sectionsDirty() {
		return cachedSections == null;
	}

	public void markDirty() {
		cachedSections = null;
	}

	public LongSet sections() {
		cachedSections = new LongOpenHashSet();
		for (var value : visuals.values()) {
			cachedSections.addAll(value.sections);
		}
		return cachedSections;
	}

	public void remove(SmoothLitVisual smoothLit) {
		visuals.remove(smoothLit);
	}

	public void add(SectionPropertyImpl tracker, SmoothLitVisual smoothLit) {
		visuals.put(smoothLit, tracker);

		tracker.addListener(this::markDirty);

		if (!tracker.sections.isEmpty()) {
			markDirty();
		}
	}

	public void clear() {
		visuals.clear();
	}

}
