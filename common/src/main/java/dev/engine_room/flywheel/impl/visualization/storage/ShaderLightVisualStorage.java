package dev.engine_room.flywheel.impl.visualization.storage;

import java.util.Map;

import dev.engine_room.flywheel.api.visual.ShaderLightVisual;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import it.unimi.dsi.fastutil.longs.LongSet;
import it.unimi.dsi.fastutil.objects.Reference2ReferenceOpenHashMap;

public class ShaderLightVisualStorage {
	private final Map<ShaderLightVisual, SectionTracker> trackers = new Reference2ReferenceOpenHashMap<>();

	private final LongSet sections = new LongOpenHashSet();
	private boolean isDirty;

	public final Runnable sectionListener = this::markDirty;

	public LongSet sections() {
		if (isDirty) {
			sections.clear();
			for (var tracker : trackers.values()) {
				sections.addAll(tracker.sections());
			}
			isDirty = false;
		}
		return sections;
	}

	public boolean isDirty() {
		return isDirty;
	}

	public void markDirty() {
		isDirty = true;
	}

	public void add(ShaderLightVisual visual, SectionTracker tracker) {
		trackers.put(visual, tracker);

		tracker.addListener(sectionListener);

		if (!tracker.sections().isEmpty()) {
			markDirty();
		}
	}

	public void remove(ShaderLightVisual visual) {
		var tracker = trackers.remove(visual);

		if (tracker != null) {
			markDirty();
		}
	}

	public void clear() {
		trackers.clear();
		markDirty();
	}
}
