package com.jozufozu.flywheel.backend.engine;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.List;

import com.jozufozu.flywheel.api.instancer.InstancePart;
import com.jozufozu.flywheel.api.instancer.Instancer;
import com.jozufozu.flywheel.api.struct.StructType;

public abstract class AbstractInstancer<P extends InstancePart> implements Instancer<P> {

	public final StructType<P> type;

	// Lock for all instance data, only needs to be used in methods that may run on the TaskExecutor.
	protected final Object lock = new Object();
	protected final ArrayList<P> data = new ArrayList<>();
	protected final ArrayList<HandleImpl> handles = new ArrayList<>();

	// TODO: atomic bitset?
	protected final BitSet changed = new BitSet();
	protected final BitSet deleted = new BitSet();

	protected AbstractInstancer(StructType<P> type) {
		this.type = type;
	}

	/**
	 * @return a handle to a new copy of this model.
	 */
	@Override
	public P createInstance() {
		synchronized (lock) {
			var i = data.size();
			var handle = new HandleImpl(this, i);
			P instanceData = type.create(handle);

			data.add(instanceData);
			handles.add(handle);
			changed.set(i);
			return instanceData;
		}
	}

	/**
	 * Clear all instance data without freeing resources.
	 */
	public void clear() {
		handles.forEach(HandleImpl::clear);
		data.clear();
		handles.clear();
		changed.clear();
		deleted.clear();
	}

	public int getInstanceCount() {
		return data.size();
	}

	public List<P> getRange(int start, int end) {
		return data.subList(start, end);
	}

	public List<P> getAll() {
		return data;
	}

	protected void removeDeletedInstances() {
		// Figure out which elements are to be removed.
		final int oldSize = this.data.size();
		int removeCount = deleted.cardinality();

		final int newSize = oldSize - removeCount;

		// shift surviving elements left over the spaces left by removed elements
		for (int i = 0, j = 0; (i < oldSize) && (j < newSize); i++, j++) {
			i = deleted.nextClearBit(i);

			if (i != j) {
				var handle = handles.get(i);
				P element = data.get(i);

				handles.set(j, handle);
				data.set(j, element);

				handle.setIndex(j);
				changed.set(j);
			}
		}

		deleted.clear();
		data.subList(newSize, oldSize)
				.clear();
		handles.subList(newSize, oldSize)
				.clear();
	}

	@Override
	public String toString() {
		return "Instancer[" + getInstanceCount() + ']';
	}

	public void notifyDirty(int index) {
		if (index < 0 || index >= getInstanceCount()) {
			return;
		}
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
}
