package com.jozufozu.flywheel.impl.visualization.storage;

import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.WeakHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicLong;

import com.jozufozu.flywheel.api.task.Plan;
import com.jozufozu.flywheel.api.task.TaskExecutor;
import com.jozufozu.flywheel.api.visual.LitVisual;
import com.jozufozu.flywheel.api.visual.VisualFrameContext;
import com.jozufozu.flywheel.lib.task.PlanUtil;
import com.jozufozu.flywheel.lib.task.SimplyComposedPlan;
import com.jozufozu.flywheel.lib.task.Synchronizer;

import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.longs.LongArraySet;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import it.unimi.dsi.fastutil.longs.LongSet;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;

/**
 * Keeps track of what chunks/sections each listener is in, so we can update exactly what needs to be updated.
 */
public class LitVisualStorage {
	private static final long NEVER_UPDATED = Long.MIN_VALUE;
	private static final long INITIAL_UPDATE_ID = NEVER_UPDATED + 1;

	private final Map<LitVisual, LongSet> visuals2Sections = new WeakHashMap<>();
	private final Long2ObjectMap<List<Updater>> sections2Visuals = new Long2ObjectOpenHashMap<>();

	private final Queue<LitVisual> movedVisuals = new ConcurrentLinkedQueue<>();
	private final LongSet sectionsUpdatedThisFrame = new LongOpenHashSet();

	private long updateId = INITIAL_UPDATE_ID;

	public Plan<VisualFrameContext> plan() {
		return (SimplyComposedPlan<VisualFrameContext>) (TaskExecutor taskExecutor, VisualFrameContext context, Runnable onCompletion) -> {
			processMoved();

			if (sectionsUpdatedThisFrame.isEmpty()) {
				onCompletion.run();
				return;
			}

			var sync = new Synchronizer(sectionsUpdatedThisFrame.size(), () -> {
				sectionsUpdatedThisFrame.clear();
				onCompletion.run();
			});

			long updateId = getNextUpdateId();

			for (long section : sectionsUpdatedThisFrame) {
				var visuals = sections2Visuals.get(section);
				if (visuals != null && !visuals.isEmpty()) {
					taskExecutor.execute(() -> PlanUtil.distribute(taskExecutor, updateId, sync, visuals, Updater::updateLight));
				} else {
					sync.decrementAndEventuallyRun();
				}
			}
		};
	}

	private void processMoved() {
		LitVisual visual;
		while ((visual = movedVisuals.poll()) != null) {
			// If the visual isn't there when we try to remove it that means it was deleted before we got to it.
			if (remove(visual)) {
				add(visual);
			}
		}
	}

	private long getNextUpdateId() {
		long out = this.updateId;

		this.updateId++;
		if (this.updateId == NEVER_UPDATED) {
			// Somehow we were running long enough to wrap around. Go back to the initial value.
			this.updateId = INITIAL_UPDATE_ID;
		}

		return out;
	}

	public boolean isEmpty() {
		return visuals2Sections.isEmpty();
	}

	public void addAndInitNotifier(LitVisual visual) {
		visual.initLightSectionNotifier(new LitVisualNotifierImpl(visual));
		add(visual);
	}

	private void add(LitVisual visual) {
		LongSet sections = new LongArraySet();

		visual.collectLightSections(sections::add);

		// Add the visual to the map even if sections is empty, this way we can distinguish from deleted visuals
		visuals2Sections.put(visual, sections);

		// Don't bother creating an updater if the visual isn't in any sections.
		if (sections.isEmpty()) {
			return;
		}

		var updater = createUpdater(visual, sections.size());

		for (long section : sections) {
			sections2Visuals.computeIfAbsent(section, $ -> new ObjectArrayList<>())
					.add(updater);
		}
	}

	public void enqueueLightUpdateSections(LongSet sections) {
		sectionsUpdatedThisFrame.addAll(sections);
	}

	/**
	 * Remove the visual from this storage.
	 *
	 * @param visual The visual to remove.
	 * @return {@code true} if the visual was removed, {@code false} otherwise.
	 */
	public boolean remove(LitVisual visual) {
		var sections = visuals2Sections.remove(visual);

		if (sections == null) {
			return false;
		}

		for (long section : sections) {
			List<Updater> listeners = sections2Visuals.get(section);
			if (listeners != null) {
				listeners.remove(indexOfUpdater(listeners, visual));
			}
		}

		return true;
	}

	public void clear() {
		visuals2Sections.clear();
		sections2Visuals.clear();
		sectionsUpdatedThisFrame.clear();
	}

	private static int indexOfUpdater(List<Updater> listeners, LitVisual visual) {
		for (int i = 0; i < listeners.size(); i++) {
			if (listeners.get(i)
					.visual() == visual) {
				return i;
			}
		}
		return -1;
	}

	private static Updater createUpdater(LitVisual visual, int sectionCount) {
		if (sectionCount == 1) {
			return new Updater.Simple(visual);
		} else {
			return new Updater.Synced(visual, new AtomicLong(NEVER_UPDATED));
		}
	}

	// Breaking this into 2 separate cases allows us to avoid the sync overhead in the common case.
	// TODO: is it faster to only use the synced variant to avoid virtual dispatches?
	sealed interface Updater {
		void updateLight(long updateId);

		LitVisual visual();

		// The visual is only in one section. In this case, we can just update the visual directly.
		record Simple(LitVisual visual) implements Updater {
			@Override
			public void updateLight(long updateId) {
				visual.updateLight();
			}
		}

		// The visual is in multiple sections. Here we need to make sure that the visual only gets updated once,
		// even when multiple sections it was contained in are updated at the same time.
		record Synced(LitVisual visual, AtomicLong updateId) implements Updater {
			@Override
			public void updateLight(long updateId) {
				// Different update ID means we won, so we can update the visual.
				// Same update ID means another thread beat us to the update.
				if (this.updateId.getAndSet(updateId) != updateId) {
					visual.updateLight();
				}
			}
		}
	}

	private final class LitVisualNotifierImpl implements LitVisual.Notifier {
		private final LitVisual litVisual;

		private LitVisualNotifierImpl(LitVisual litVisual) {
			this.litVisual = litVisual;
		}

		@Override
		public void notifySectionsChanged() {
			movedVisuals.add(litVisual);
        }
    }
}
