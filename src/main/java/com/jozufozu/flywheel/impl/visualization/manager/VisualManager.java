package com.jozufozu.flywheel.impl.visualization.manager;

import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.function.Consumer;

import org.joml.FrustumIntersection;

import com.jozufozu.flywheel.api.task.TaskExecutor;
import com.jozufozu.flywheel.api.visual.DynamicVisual;
import com.jozufozu.flywheel.api.visual.TickableVisual;
import com.jozufozu.flywheel.api.visual.Visual;
import com.jozufozu.flywheel.config.FlwConfig;
import com.jozufozu.flywheel.impl.visualization.ratelimit.BandedPrimeLimiter;
import com.jozufozu.flywheel.impl.visualization.ratelimit.DistanceUpdateLimiter;
import com.jozufozu.flywheel.impl.visualization.ratelimit.NonLimiter;
import com.jozufozu.flywheel.impl.visualization.storage.Storage;
import com.jozufozu.flywheel.impl.visualization.storage.Transaction;

public abstract class VisualManager<T> {
	private final Queue<Transaction<T>> queue = new ConcurrentLinkedQueue<>();

	protected DistanceUpdateLimiter tickLimiter;
	protected DistanceUpdateLimiter frameLimiter;

	public VisualManager() {
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
	 * Get the number of game objects that are currently being visualized.
	 *
	 * @return The object count.
	 */
	public int getVisualCount() {
		return getStorage().getAllVisuals().size();
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
	 * Update the visual associated with an object.
	 *
	 * <p>
	 *     By default this is the only hook a {@link Visual} has to change its internal state. This is the lowest frequency
	 *     update hook {@link Visual} gets. For more frequent updates, see {@link TickableVisual} and
	 *     {@link DynamicVisual}.
	 * </p>
	 *
	 * @param obj the object whose visual will be updated.
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
	 * Ticks the VisualManager.
	 *
	 * <p>
	 *     {@link TickableVisual}s get ticked.
	 *     <br>
	 *     Queued updates are processed.
	 * </p>
	 */
	public void tick(TaskExecutor executor, double cameraX, double cameraY, double cameraZ) {
		tickLimiter.tick();
		processQueue();

		var visuals = getStorage().getTickableVisuals();
		distributeWork(executor, visuals, visual -> tickVisual(visual, cameraX, cameraY, cameraZ));
	}

	protected void tickVisual(TickableVisual visual, double cameraX, double cameraY, double cameraZ) {
		if (!visual.decreaseTickRateWithDistance() || tickLimiter.shouldUpdate(visual.distanceSquared(cameraX, cameraY, cameraZ))) {
			visual.tick();
		}
	}

	public void beginFrame(TaskExecutor executor, double cameraX, double cameraY, double cameraZ, FrustumIntersection frustum) {
		frameLimiter.tick();
		processQueue();

		var visuals = getStorage().getDynamicVisuals();
		distributeWork(executor, visuals, visual -> updateVisual(visual, cameraX, cameraY, cameraZ, frustum));
	}

	protected void updateVisual(DynamicVisual visual, double cameraX, double cameraY, double cameraZ, FrustumIntersection frustum) {
		if (!visual.decreaseFramerateWithDistance() || frameLimiter.shouldUpdate(visual.distanceSquared(cameraX, cameraY, cameraZ))) {
			if (visual.isVisible(frustum)) {
				visual.beginFrame();
			}
		}
	}

	private static <V> void distributeWork(TaskExecutor executor, List<V> visuals, Consumer<V> action) {
		final int amount = visuals.size();
		final int threadCount = executor.getThreadCount();

		if (threadCount == 1) {
			executor.execute(() -> visuals.forEach(action));
		} else {
			final int stride = Math.max(amount / (threadCount * 2), 1);
			for (int start = 0; start < amount; start += stride) {
				int end = Math.min(start + stride, amount);

				var sub = visuals.subList(start, end);
				executor.execute(() -> sub.forEach(action));
			}
		}
	}
}
