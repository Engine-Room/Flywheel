package com.jozufozu.flywheel.backend.engine;

import java.util.ArrayList;

import org.jetbrains.annotations.Nullable;

import com.jozufozu.flywheel.api.instance.Instance;
import com.jozufozu.flywheel.api.instance.InstanceType;
import com.jozufozu.flywheel.api.instance.Instancer;
import com.jozufozu.flywheel.backend.engine.embed.Environment;
import com.jozufozu.flywheel.lib.util.AtomicBitset;

public abstract class AbstractInstancer<I extends Instance> implements Instancer<I> {
	public final InstanceType<I> type;
	public final Environment environment;

	// Lock for all instances, only needs to be used in methods that may run on the TaskExecutor.
	protected final Object lock = new Object();
	protected final ArrayList<I> instances = new ArrayList<>();
	protected final ArrayList<InstanceHandleImpl> handles = new ArrayList<>();

	protected final AtomicBitset changed = new AtomicBitset();
	protected final AtomicBitset deleted = new AtomicBitset();

	protected AbstractInstancer(InstanceType<I> type, Environment environment) {
		this.type = type;
		this.environment = environment;

		environment.acquire();
	}

	@Override
	public I createInstance() {
		synchronized (lock) {
			var i = instances.size();
			var handle = new InstanceHandleImpl(this, i);
			I instance = type.create(handle);

			addLocked(instance, handle);
			return instance;
		}
	}

	@Override
	public void stealInstance(@Nullable I instance) {
		if (instance == null) {
			return;
		}

		var instanceHandle = instance.handle();

		if (!(instanceHandle instanceof InstanceHandleImpl handle)) {
			// UB: do nothing
			return;
		}

		if (handle.instancer == this) {
			return;
		}

		// FIXME: in theory there could be a race condition here if the instance
		//  is somehow being stolen by 2 different instancers between threads.
		//  That seems kinda impossible so I'm fine leaving it as is for now.

		// Remove the instance from its old instancer.
		// This won't have any unwanted effect when the old instancer
		// is filtering deleted instances later, so is safe.
		handle.setDeleted();

		// Only lock now that we'll be mutating our state.
		synchronized (lock) {
			// Add the instance to this instancer.
			handle.instancer = this;
			handle.index = instances.size();
			addLocked(instance, handle);
		}
	}

	/**
	 * Calls must be synchronized on {@link #lock}.
	 */
	private void addLocked(I instance, InstanceHandleImpl handle) {
		instances.add(instance);
		handles.add(handle);
		changed.set(handle.index);
	}

	public int instanceCount() {
		return instances.size();
	}

	public void notifyDirty(int index) {
		if (index < 0 || index >= instanceCount()) {
			return;
		}
		changed.set(index);
	}

	public void notifyRemoval(int index) {
		if (index < 0 || index >= instanceCount()) {
			return;
		}
		deleted.set(index);
	}

	protected void removeDeletedInstances() {
		if (deleted.isEmpty()) {
			return;
		}

		// Figure out which elements are to be removed.
		final int oldSize = this.instances.size();
		int removeCount = deleted.cardinality();

		if (oldSize == removeCount) {
			clear();
			return;
		}

		final int newSize = oldSize - removeCount;

		// Start from the first deleted index.
		int writePos = deleted.nextSetBit(0);

		if (writePos < newSize) {
			// Since we'll be shifting everything into this space we can consider it all changed.
			changed.set(writePos, newSize);
		}

		// We definitely shouldn't consider the deleted instances as changed though,
		// else we might try some out of bounds accesses later.
		changed.clear(newSize, oldSize);

		// Punch out the deleted instances, shifting over surviving instances to fill their place.
		for (int scanPos = writePos; (scanPos < oldSize) && (writePos < newSize); scanPos++, writePos++) {
			// Find next non-deleted element.
			scanPos = deleted.nextClearBit(scanPos);

			if (scanPos != writePos) {
				// Grab the old instance/handle from scanPos...
				var handle = handles.get(scanPos);
				I instance = instances.get(scanPos);

				// ... and move it to writePos.
				handles.set(writePos, handle);
				instances.set(writePos, instance);

				// Make sure the handle knows it's been moved
				handle.index = writePos;
			}
		}

		deleted.clear();
		instances.subList(newSize, oldSize)
				.clear();
		handles.subList(newSize, oldSize)
				.clear();
	}

	/**
	 * Clear all instances without freeing resources.
	 */
	public void clear() {
		handles.forEach(InstanceHandleImpl::clear);
		instances.clear();
		handles.clear();
		changed.clear();
		deleted.clear();
	}

	public void delete() {
		environment.release();
	}

	@Override
	public String toString() {
		return "AbstractInstancer[" + instanceCount() + ']';
	}
}
