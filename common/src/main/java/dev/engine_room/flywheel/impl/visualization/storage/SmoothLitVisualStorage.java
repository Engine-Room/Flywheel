package dev.engine_room.flywheel.impl.visualization.storage;

import java.util.Map;

import org.jetbrains.annotations.Nullable;

import dev.engine_room.flywheel.api.visual.SmoothLitVisual;
import it.unimi.dsi.fastutil.longs.LongArraySet;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import it.unimi.dsi.fastutil.longs.LongSet;
import it.unimi.dsi.fastutil.objects.Reference2ObjectOpenHashMap;

public class SmoothLitVisualStorage {
	private final Map<SmoothLitVisual, SectionProperty> visuals = new Reference2ObjectOpenHashMap<>();

	@Nullable
	private LongSet cachedSections;

	public boolean sectionsDirty() {
		return cachedSections == null;
	}

	public LongSet sections() {
		cachedSections = new LongOpenHashSet();
		for (SectionProperty value : visuals.values()) {
			cachedSections.addAll(value.sections);
		}
		return cachedSections;
	}

	public void remove(SmoothLitVisual smoothLit) {
		visuals.remove(smoothLit);
	}

	public void add(SmoothLitVisual smoothLit) {
		var sections = new SectionProperty();
		visuals.put(smoothLit, sections);
		smoothLit.setSectionProperty(sections);
	}

	public void clear() {
		visuals.clear();
	}

	private final class SectionProperty implements SmoothLitVisual.SectionProperty {
		private final LongSet sections = new LongArraySet();

		@Override
		public void lightSections(LongSet sections) {
			this.sections.clear();
			this.sections.addAll(sections);

			SmoothLitVisualStorage.this.cachedSections = null;
		}
	}
}
