package com.jozufozu.flywheel.impl.instancing.manager;

import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.function.Consumer;

import org.joml.FrustumIntersection;

import com.jozufozu.flywheel.api.instance.DynamicInstance;
import com.jozufozu.flywheel.api.instance.Instance;
import com.jozufozu.flywheel.api.instance.TickableInstance;
import com.jozufozu.flywheel.api.task.TaskExecutor;
import com.jozufozu.flywheel.config.FlwConfig;
import com.jozufozu.flywheel.impl.instancing.ratelimit.BandedPrimeLimiter;
import com.jozufozu.flywheel.impl.instancing.ratelimit.DistanceUpdateLimiter;
import com.jozufozu.flywheel.impl.instancing.ratelimit.NonLimiter;
import com.jozufozu.flywheel.impl.instancing.storage.Storage;
import com.jozufozu.flywheel.impl.instancing.storage.Transaction;

public abstract class InstanceManager<T> {
	private final Queue<Transaction<T>> queue = new ConcurrentLinkedQueue<>();

	protected DistanceUpdateLimiter tickLimiter;
	protected DistanceUpdateLimiter frameLimiter;

	public InstanceManager() {
		tickLimiter = createUpdateLimiter();
		frameLimiter = createUpdateLimiter();
	}

	protected abstract Storage<T> getStorage();

	protected DistanceUpdateLimiter createUpdateLimiter() {
		if (FlwConfig.get().limitUpdates()) {
			return new BandedPrimeLimiter();
		} else {
			return new NonLimiter();
		}
	}

	/**
	 * Get the number of game objects that are currently being instanced.
	 *
	 * @return The object count.
	 */
	public int getInstanceCount() {
		return getStorage().getAllInstances().size();
	}

	public void add(T obj) {
		if (!getStorage().willAccept(obj)) {
			return;
		}

		getStorage().add(obj);
	}

	public void queueAdd(T obj) {
		if (!getStorage().willAccept(obj)) {
			return;
		}

		queue.add(Transaction.add(obj));
	}

	public void remove(T obj) {
		getStorage().remove(obj);
	}

	public void queueRemove(T obj) {
		queue.add(Transaction.remove(obj));
	}

	/**
	 * Update the instance associated with an object.
	 *
	 * <p>
	 *     By default this is the only hook an {@link Instance} has to change its internal state. This is the lowest frequency
	 *     update hook {@link Instance} gets. For more frequent updates, see {@link TickableInstance} and
	 *     {@link DynamicInstance}.
	 * </p>
	 *
	 * @param obj the object to update.
	 */
	public void update(T obj) {
		if (!getStorage().willAccept(obj)) {
			return;
		}

		getStorage().update(obj);
	}

	public void queueUpdate(T obj) {
		if (!getStorage().willAccept(obj)) {
			return;
		}

		queue.add(Transaction.update(obj));
	}

	public void recreateAll() {
		getStorage().recreateAll();
	}

	public void invalidate() {
		getStorage().invalidate();
	}

	protected void processQueue() {
		var storage = getStorage();
		Transaction<T> transaction;
		while ((transaction = queue.poll()) != null) {
			transaction.apply(storage);
		}
	}

	/**
	 * Ticks the InstanceManager.
	 *
	 * <p>
	 *     {@link TickableInstance}s get ticked.
	 *     <br>
	 *     Queued updates are processed.
	 * </p>
	 */
	public void tick(TaskExecutor executor, double cameraX, double cameraY, double cameraZ) {
		tickLimiter.tick();
		processQueue();

		var instances = getStorage().getTickableInstances();
		distributeWork(executor, instances, instance -> tickInstance(instance, cameraX, cameraY, cameraZ));
	}

	protected void tickInstance(TickableInstance instance, double cameraX, double cameraY, double cameraZ) {
		if (!instance.decreaseTickRateWithDistance() || tickLimiter.shouldUpdate(instance.distanceSquared(cameraX, cameraY, cameraZ))) {
			instance.tick();
		}
	}

	public void beginFrame(TaskExecutor executor, double cameraX, double cameraY, double cameraZ, FrustumIntersection frustum) {
		frameLimiter.tick();
		processQueue();

		var instances = getStorage().getDynamicInstances();
		distributeWork(executor, instances, instance -> updateInstance(instance, cameraX, cameraY, cameraZ, frustum));
	}

	protected void updateInstance(DynamicInstance instance, double cameraX, double cameraY, double cameraZ, FrustumIntersection frustum) {
		if (!instance.decreaseFramerateWithDistance() || frameLimiter.shouldUpdate(instance.distanceSquared(cameraX, cameraY, cameraZ))) {
			if (instance.isVisible(frustum)) {
				instance.beginFrame();
			}
		}
	}

	private static <I> void distributeWork(TaskExecutor executor, List<I> instances, Consumer<I> action) {
		final int size = instances.size();
		final int threadCount = executor.getThreadCount();

		if (threadCount == 1) {
			executor.execute(() -> instances.forEach(action));
		} else {
			final int stride = Math.max(size / (threadCount * 2), 1);
			for (int start = 0; start < size; start += stride) {
				int end = Math.min(start + stride, size);

				var sub = instances.subList(start, end);
				executor.execute(() -> sub.forEach(action));
			}
		}
	}
}
