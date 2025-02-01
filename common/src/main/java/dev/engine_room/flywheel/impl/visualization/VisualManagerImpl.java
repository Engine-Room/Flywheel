package dev.engine_room.flywheel.impl.visualization;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;

import dev.engine_room.flywheel.api.task.Plan;
import dev.engine_room.flywheel.api.task.TaskExecutor;
import dev.engine_room.flywheel.api.visual.DynamicVisual;
import dev.engine_room.flywheel.api.visual.LightUpdatedVisual;
import dev.engine_room.flywheel.api.visual.SectionTrackedVisual;
import dev.engine_room.flywheel.api.visual.ShaderLightVisual;
import dev.engine_room.flywheel.api.visual.TickableVisual;
import dev.engine_room.flywheel.api.visual.Visual;
import dev.engine_room.flywheel.api.visualization.VisualManager;
import dev.engine_room.flywheel.impl.visualization.storage.Action;
import dev.engine_room.flywheel.impl.visualization.storage.LightUpdatedVisualStorage;
import dev.engine_room.flywheel.impl.visualization.storage.SectionTracker;
import dev.engine_room.flywheel.impl.visualization.storage.Storage;
import dev.engine_room.flywheel.impl.visualization.storage.Transaction;
import dev.engine_room.flywheel.lib.task.Distribute;
import dev.engine_room.flywheel.lib.task.SimplyComposedPlan;
import dev.engine_room.flywheel.lib.task.Synchronizer;
import it.unimi.dsi.fastutil.longs.LongSet;
import it.unimi.dsi.fastutil.objects.Reference2IntMap;
import it.unimi.dsi.fastutil.objects.Reference2IntOpenHashMap;
import net.minecraft.util.Unit;

public class VisualManagerImpl<T, S extends Storage<T>> implements VisualManager<T> {
	private final ConcurrentLinkedQueue<Transaction<T>> queue = new ConcurrentLinkedQueue<>();

	private final S storage;

	public VisualManagerImpl(S storage) {
		this.storage = storage;
	}

	public S getStorage() {
		return storage;
	}

	@Override
	public int visualCount() {
		return getStorage().getAllVisuals()
				.size();
	}

	@Override
	public void queueAdd(T obj) {
		if (!getStorage().willAccept(obj)) {
			return;
		}

		queue.add(Transaction.add(obj));
	}

	@Override
	public void queueRemove(T obj) {
		if (!getStorage().willAccept(obj)) {
			return;
		}

		queue.add(Transaction.remove(obj));
	}

	@Override
	public void queueUpdate(T obj) {
		if (!getStorage().willAccept(obj)) {
			return;
		}

		queue.add(Transaction.update(obj));
	}

	public Plan<DynamicVisual.Context> framePlan() {
		return new ProcessQueuePlan<>(DynamicVisual.Context::partialTick)
				.then(storage.framePlan());
	}

	public Plan<TickableVisual.Context> tickPlan() {
		return new ProcessQueuePlan<TickableVisual.Context>($ -> 1)
				.then(storage.tickPlan());
	}

	public void onLightUpdate(long section) {
		getStorage().lightUpdatedVisuals()
				.onLightUpdate(section);
	}

	public boolean areGpuLightSectionsDirty() {
		return getStorage().shaderLightVisuals()
				.isDirty();
	}

	public LongSet gpuLightSections() {
		return getStorage().shaderLightVisuals()
				.sections();
	}

	public void invalidate() {
		getStorage().invalidate();
	}

	interface PartialTick<C> {
		float partialTick(C context);
	}

	private class ProcessQueuePlan<C> implements SimplyComposedPlan<C> {
		private final PartialTick<C> partialTick;

		private ProcessQueuePlan(PartialTick<C> partialTick) {
			this.partialTick = partialTick;
		}

		@Override
		public void execute(TaskExecutor taskExecutor, C context, Runnable onCompletion) {
			var added = new ArrayList<T>();
			var removed = new ArrayList<T>();
			var updated = new ArrayList<T>();

			sortQueue(added, removed, updated);

			var size = added.size();
			var visualDst = new Visual[size];
			var trackerDst = new SectionTracker[size];

			var partialTick = this.partialTick.partialTick(context);

			var applyPatch = new Synchronizer(2, () -> {
				var storage = getStorage();
				for (int i = 0; i < size; i++) {
					var visual = visualDst[i];

					if (visual == null) {
						continue;
					}

					var obj = added.get(i);
					var tracker = trackerDst[i];

					storage.add(obj, visual, tracker);
				}

				Distribute.tasks(taskExecutor, Unit.INSTANCE, onCompletion, updated, (obj, ignored) -> {
					storage.update(obj, 1);
				});
			});

			taskExecutor.execute(() -> {
				for (var t : removed) {
					storage.remove(t);
				}

				applyPatch.decrementAndEventuallyRun();
			});

			Distribute.indexed(taskExecutor, Unit.INSTANCE, applyPatch, added, (i, obj, ignored) -> {
				asyncCreate(i, obj, partialTick, trackerDst, visualDst);
			});
		}
	}

	private void sortQueue(List<T> added, List<T> removed, List<T> updated) {
		Reference2IntMap<T> dedupe = new Reference2IntOpenHashMap<>();

		Transaction<T> transaction;
		while ((transaction = queue.poll()) != null) {
			var i = dedupe.getInt(transaction.obj());
			dedupe.put(transaction.obj(), transaction.action().setBit(i));
		}

		for (var entry : dedupe.reference2IntEntrySet()) {
			var obj = entry.getKey();
			var action = entry.getIntValue();

			var add = Action.ADD.in(action);
			var remove = Action.REMOVE.in(action);
			var update = Action.UPDATE.in(action);

			// Add and remove can happen at the same time
			if (add) {
				added.add(obj);
			}
			if (remove) {
				removed.add(obj);
			}

			// But only update if the object will actually be around for it
			if (update && add == remove) {
				updated.add(obj);
			}
		}
	}

	private void asyncCreate(int i, T obj, float partialTick, SectionTracker[] trackerDst, Visual[] visualDst) {
		var visual = storage.createRaw(obj, partialTick);

		if (visual instanceof SectionTrackedVisual tracked) {
			var tracker = new SectionTracker();

			// Give the visual a chance to invoke the collector.
			tracked.setSectionCollector(tracker);

			if (visual instanceof LightUpdatedVisual lightUpdated) {
				lightUpdated.updateLight(partialTick);

				tracker.addListener(new LightUpdatedVisualStorage.MovedVisual(lightUpdated, tracker, storage.lightUpdatedVisuals()));
			}

			if (visual instanceof ShaderLightVisual) {
				tracker.addListener(storage.shaderLightVisuals().sectionListener);
			}

			trackerDst[i] = tracker;
		}

		visualDst[i] = visual;
	}
}
