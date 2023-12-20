package com.jozufozu.flywheel.backend.engine;

import java.util.ArrayList;

import com.jozufozu.flywheel.api.instance.Instance;
import com.jozufozu.flywheel.api.instance.InstanceType;
import com.jozufozu.flywheel.api.instance.Instancer;
import com.jozufozu.flywheel.lib.util.AtomicBitset;

public abstract class AbstractInstancer<I extends Instance> implements Instancer<I> {
	public final InstanceType<I> type;

	// Lock for all instances, only needs to be used in methods that may run on the TaskExecutor.
	protected final Object lock = new Object();
	protected final ArrayList<I> instances = new ArrayList<>();
	protected final ArrayList<InstanceHandleImpl> handles = new ArrayList<>();

	protected final AtomicBitset changed = new AtomicBitset();
	protected final AtomicBitset deleted = new AtomicBitset();

	protected AbstractInstancer(InstanceType<I> type) {
		this.type = type;
	}

	@Override
	public I createInstance() {
		synchronized (lock) {
			var i = instances.size();
			var handle = new InstanceHandleImpl(this, i);
			I instance = type.create(handle);

			instances.add(instance);
			handles.add(handle);
			changed.set(i);
			return instance;
		}
	}

	public int getInstanceCount() {
		return instances.size();
	}

	public void notifyDirty(int index) {
		if (index < 0 || index >= getInstanceCount()) {
			return;
		}
		changed.set(index);
	}

	public void notifyRemoval(int index) {
		if (index < 0 || index >= getInstanceCount()) {
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

		// Punch out the deleted instances, shifting over surviving instances to fill their place.
		for (int scanPos = 0, writePos = 0; (scanPos < oldSize) && (writePos < newSize); scanPos++, writePos++) {
			// Find next non-deleted element.
			scanPos = deleted.nextClearBit(scanPos);

			if (scanPos != writePos) {
				// Grab the old instance/handle from scanPos...
				var handle = handles.get(scanPos);
				I instance = instances.get(scanPos);

				// ... and move it to writePos.
				handles.set(writePos, handle);
				instances.set(writePos, instance);

				// Make sure the handle knows it's been moved...
				handle.index = writePos;
				// ...and set it changed to force an upload.
				changed.set(writePos);

				// Clear the old slot. There's nothing meaningful there that can be considered "changed".
				changed.clear(scanPos);
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
	}

	@Override
	public String toString() {
		return "AbstractInstancer[" + getInstanceCount() + ']';
	}
}
