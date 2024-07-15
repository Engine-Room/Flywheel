package dev.engine_room.flywheel.impl.visualization.storage;

import java.util.Map;

import org.jetbrains.annotations.Nullable;

import dev.engine_room.flywheel.api.visual.ShaderLightVisual;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import it.unimi.dsi.fastutil.longs.LongSet;
import it.unimi.dsi.fastutil.objects.Reference2ObjectOpenHashMap;

public class ShaderLightStorage {
	private final Map<ShaderLightVisual, SectionCollectorImpl> visuals = new Reference2ObjectOpenHashMap<>();

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

	public void remove(ShaderLightVisual visual) {
		visuals.remove(visual);
	}

	public void add(SectionCollectorImpl tracker, ShaderLightVisual visual) {
		visuals.put(visual, tracker);

		tracker.addListener(this::markDirty);

		if (!tracker.sections.isEmpty()) {
			markDirty();
		}
	}

	public void clear() {
		visuals.clear();
	}

}
