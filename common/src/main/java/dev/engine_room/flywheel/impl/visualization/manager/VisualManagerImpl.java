package dev.engine_room.flywheel.impl.visualization.manager;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

import dev.engine_room.flywheel.api.task.Plan;
import dev.engine_room.flywheel.api.visual.DynamicVisual;
import dev.engine_room.flywheel.api.visual.TickableVisual;
import dev.engine_room.flywheel.api.visualization.VisualManager;
import dev.engine_room.flywheel.impl.visualization.storage.Storage;
import dev.engine_room.flywheel.impl.visualization.storage.Transaction;
import dev.engine_room.flywheel.lib.task.SimplePlan;
import it.unimi.dsi.fastutil.longs.LongSet;

public class VisualManagerImpl<T, S extends Storage<T>> implements VisualManager<T> {
	private final Queue<Transaction<T>> queue = new ConcurrentLinkedQueue<>();

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
		queue.add(Transaction.remove(obj));
	}

	@Override
	public void queueUpdate(T obj) {
		if (!getStorage().willAccept(obj)) {
			return;
		}

		queue.add(Transaction.update(obj));
	}

	public void processQueue(float partialTick) {
		var storage = getStorage();
		Transaction<T> transaction;
		while ((transaction = queue.poll()) != null) {
			transaction.apply(storage, partialTick);
		}
	}

	public Plan<DynamicVisual.Context> framePlan() {
		return SimplePlan.<DynamicVisual.Context>of(context -> processQueue(context.partialTick()))
				.then(storage.framePlan());
	}

	public Plan<TickableVisual.Context> tickPlan() {
		return SimplePlan.<TickableVisual.Context>of(context -> processQueue(1))
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
}
