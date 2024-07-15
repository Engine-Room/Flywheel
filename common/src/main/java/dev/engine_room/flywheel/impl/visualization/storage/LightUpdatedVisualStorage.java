package dev.engine_room.flywheel.impl.visualization.storage;

import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.WeakHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicLong;

import dev.engine_room.flywheel.api.task.Plan;
import dev.engine_room.flywheel.api.task.TaskExecutor;
import dev.engine_room.flywheel.api.visual.DynamicVisual;
import dev.engine_room.flywheel.api.visual.LightUpdatedVisual;
import dev.engine_room.flywheel.lib.task.Distribute;
import dev.engine_room.flywheel.lib.task.SimplyComposedPlan;
import dev.engine_room.flywheel.lib.task.Synchronizer;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import it.unimi.dsi.fastutil.longs.LongSet;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;

/**
 * Keeps track of what chunks/sections each listener is in, so we can update exactly what needs to be updated.
 */
public class LightUpdatedVisualStorage {
	private static final long NEVER_UPDATED = Long.MIN_VALUE;
	private static final long INITIAL_UPDATE_ID = NEVER_UPDATED + 1;

	private final Map<LightUpdatedVisual, LongSet> visual2Sections = new WeakHashMap<>();
	private final Long2ObjectMap<List<Updater>> section2Updaters = new Long2ObjectOpenHashMap<>();

	private final LongSet sectionsUpdatedThisFrame = new LongOpenHashSet();
	private final Queue<MovedVisual> movedVisuals = new ConcurrentLinkedQueue<>();

	private long updateId = INITIAL_UPDATE_ID;

	public Plan<DynamicVisual.Context> plan() {
		return (SimplyComposedPlan<DynamicVisual.Context>) (TaskExecutor taskExecutor, DynamicVisual.Context context, Runnable onCompletion) -> {
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
			Updater.Context updaterContext = new Updater.Context(updateId, context.partialTick());

			for (long section : sectionsUpdatedThisFrame) {
				var updaters = section2Updaters.get(section);
				if (updaters != null && !updaters.isEmpty()) {
					taskExecutor.execute(() -> Distribute.tasks(taskExecutor, updaterContext, sync, updaters, Updater::updateLight));
				} else {
					sync.decrementAndEventuallyRun();
				}
			}
		};
	}

	private void processMoved() {
		MovedVisual moved;
		while ((moved = movedVisuals.poll()) != null) {
			// If the visual isn't there when we try to remove it that means it was deleted before we got to it.
			if (remove(moved.visual)) {
				addInner(moved.visual, moved.tracker);
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

	public void add(LightUpdatedVisual visual, SectionTracker tracker) {
		var moved = new MovedVisual(visual, tracker);
		tracker.addListener(() -> movedVisuals.add(moved));

		addInner(visual, tracker);
	}

	private void addInner(LightUpdatedVisual visual, SectionTracker tracker) {
		if (tracker.sections().isEmpty()) {
			// Add the visual to the map even if sections is empty, this way we can distinguish from deleted visuals
			visual2Sections.put(visual, LongSet.of());

			// Don't bother creating an updater if the visual isn't in any sections.
			return;
		}

		var sections = tracker.sections();
		visual2Sections.put(visual, sections);
		var updater = createUpdater(visual, sections.size());

		for (long section : sections) {
			section2Updaters.computeIfAbsent(section, $ -> new ObjectArrayList<>())
					.add(updater);
		}
	}

	/**
	 * Remove the visual from this storage.
	 *
	 * @param visual The visual to remove.
	 * @return {@code true} if the visual was removed, {@code false} otherwise.
	 */
	public boolean remove(LightUpdatedVisual visual) {
		var sections = visual2Sections.remove(visual);

		if (sections == null) {
			return false;
		}

		for (long section : sections) {
			List<Updater> updaters = section2Updaters.get(section);
			if (updaters != null) {
				updaters.remove(indexOfUpdater(updaters, visual));
			}
		}

		return true;
	}

	public void onLightUpdate(long section) {
		sectionsUpdatedThisFrame.add(section);
	}

	public void clear() {
		visual2Sections.clear();
		section2Updaters.clear();
		sectionsUpdatedThisFrame.clear();
		movedVisuals.clear();
	}

	private static int indexOfUpdater(List<Updater> updaters, LightUpdatedVisual visual) {
		for (int i = 0; i < updaters.size(); i++) {
			if (updaters.get(i)
					.visual() == visual) {
				return i;
			}
		}
		return -1;
	}

	private static Updater createUpdater(LightUpdatedVisual visual, int sectionCount) {
		if (sectionCount == 1) {
			return new Updater.Simple(visual);
		} else {
			return new Updater.Synced(visual, new AtomicLong(NEVER_UPDATED));
		}
	}

	// Breaking this into 2 separate cases allows us to avoid the overhead of atomics in the common case.
	private sealed interface Updater {
		void updateLight(Context ctx);

		LightUpdatedVisual visual();

		// The visual is only in one section. In this case, we can just update the visual directly.
		record Simple(LightUpdatedVisual visual) implements Updater {
			@Override
			public void updateLight(Context ctx) {
				visual.updateLight(ctx.partialTick);
			}
		}

		// The visual is in multiple sections. Here we need to make sure that the visual only gets updated once,
		// even when multiple sections it was contained in are updated at the same time.
		record Synced(LightUpdatedVisual visual, AtomicLong updateId) implements Updater {
			@Override
			public void updateLight(Context ctx) {
				// Different update ID means we won, so we can update the visual.
				// Same update ID means another thread beat us to the update.
				if (this.updateId.getAndSet(ctx.updateId) != ctx.updateId) {
					visual.updateLight(ctx.partialTick);
				}
			}
		}

		record Context(long updateId, float partialTick) {
		}
	}

	private record MovedVisual(LightUpdatedVisual visual, SectionTracker tracker) {
	}
}
