package com.jozufozu.flywheel.backend.engine;

import java.util.ArrayList;
import java.util.BitSet;

import com.jozufozu.flywheel.api.instance.Instance;
import com.jozufozu.flywheel.api.instance.InstanceType;
import com.jozufozu.flywheel.api.instance.Instancer;

public abstract class AbstractInstancer<I extends Instance> implements Instancer<I> {
	public final InstanceType<I> type;

	// Lock for all instances, only needs to be used in methods that may run on the TaskExecutor.
	protected final Object lock = new Object();
	protected final ArrayList<I> instances = new ArrayList<>();
	protected final ArrayList<InstanceHandleImpl> handles = new ArrayList<>();

	protected final BitSet changed = new BitSet();
	protected final BitSet deleted = new BitSet();

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
		// TODO: Atomic bitset. Synchronizing here blocks the task executor and causes massive overhead.
		synchronized (lock) {
			changed.set(index);
		}
	}

	public void notifyRemoval(int index) {
		if (index < 0 || index >= getInstanceCount()) {
			return;
		}
		synchronized (lock) {
			deleted.set(index);
		}
	}

	protected void removeDeletedInstances() {
		if (deleted.isEmpty()) {
			return;
		}

		// Figure out which elements are to be removed.
		final int oldSize = this.instances.size();
		int removeCount = deleted.cardinality();

		final int newSize = oldSize - removeCount;

		// shift surviving elements left over the spaces left by removed elements
		for (int i = 0, j = 0; (i < oldSize) && (j < newSize); i++, j++) {
			i = deleted.nextClearBit(i);

			if (i != j) {
				var handle = handles.get(i);
				I instance = instances.get(i);

				handles.set(j, handle);
				instances.set(j, instance);

				handle.index = j;
				changed.set(j);
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
