package com.jozufozu.flywheel.impl.visualization.manager;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

import com.jozufozu.flywheel.api.task.Plan;
import com.jozufozu.flywheel.api.visualization.VisualManager;
import com.jozufozu.flywheel.impl.visualization.storage.Storage;
import com.jozufozu.flywheel.impl.visualization.storage.Transaction;
import com.jozufozu.flywheel.lib.task.SimplePlan;

public class VisualManagerImpl<T, S extends Storage<T>> implements VisualManager<T> {
	private final Queue<Transaction<T>> queue = new ConcurrentLinkedQueue<>();

	protected final S storage;

	public VisualManagerImpl(S storage) {
		this.storage = storage;
	}

	public S getStorage() {
		return storage;
	}

	@Override
	public int getVisualCount() {
		return getStorage().getAllVisuals().size();
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

	public Plan<Float> createRecreationPlan() {
		return SimplePlan.of(getStorage()::recreateAll);
	}

	public void invalidate() {
		getStorage().invalidate();
	}

	public void processQueue(float partialTick) {
		var storage = getStorage();
		Transaction<T> transaction;
		while ((transaction = queue.poll()) != null) {
			transaction.apply(storage, partialTick);
		}
	}

}
