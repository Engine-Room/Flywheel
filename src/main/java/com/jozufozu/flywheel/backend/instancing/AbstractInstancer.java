package com.jozufozu.flywheel.backend.instancing;

import java.util.ArrayList;
import java.util.BitSet;

import com.jozufozu.flywheel.backend.struct.StructType;
import com.jozufozu.flywheel.core.model.IModel;

public class AbstractInstancer<D extends InstanceData> implements Instancer<D> {

	protected final StructType<D> type;
	protected final IModel modelData;
	protected final ArrayList<D> data = new ArrayList<>();

	boolean anyToRemove;
	boolean anyToUpdate;

	public AbstractInstancer(StructType<D> type, IModel modelData) {
		this.type = type;
		this.modelData = modelData;
	}

	/**
	 * @return a handle to a new copy of this model.
	 */
	@Override
	public D createInstance() {
		D data = type.create();
		data.owner = this;
		return _add(data);
	}

	/**
	 * Copy a data from another Instancer to this.
	 *
	 * This has the effect of swapping out one model for another.
	 * @param inOther the data associated with a different model.
	 */
	@Override
	public void stealInstance(D inOther) {
		if (inOther.owner == this) return;

		inOther.delete();
		// sike, we want to keep it, changing the owner reference will still delete it in the other
		inOther.removed = false;
		_add(inOther);
	}

	@Override
	public void markDirty(InstanceData instanceData) {
		anyToUpdate = true;
		instanceData.dirty = true;
	}

	@Override
	public void markRemoval(InstanceData instanceData) {
		anyToRemove = true;
		instanceData.removed = true;
	}

	/**
	 * Clear all instance data without freeing resources.
	 */
	public void clear() {
		data.clear();
		anyToRemove = true;
	}

	protected BitSet getDirtyBitSet() {
		final int size = data.size();
		final BitSet dirtySet = new BitSet(size);

		for (int i = 0; i < size; i++) {
			D element = data.get(i);
			if (element.dirty) {
				dirtySet.set(i);

				element.dirty = false;
			}
		}
		return dirtySet;
	}

	protected void removeDeletedInstances() {
		// Figure out which elements are to be removed.
		final int oldSize = this.data.size();
		int removeCount = 0;
		final BitSet removeSet = new BitSet(oldSize);
		for (int i = 0; i < oldSize; i++) {
			final D element = this.data.get(i);
			if (element.removed || element.owner != this) {
				removeSet.set(i);
				removeCount++;
			}
		}

		final int newSize = oldSize - removeCount;

		// shift surviving elements left over the spaces left by removed elements
		for (int i = 0, j = 0; (i < oldSize) && (j < newSize); i++, j++) {
			i = removeSet.nextClearBit(i);

			if (i != j) {
				D element = data.get(i);
				data.set(j, element);
				element.dirty = true;
			}
		}

		anyToUpdate = true;

		data.subList(newSize, oldSize)
				.clear();

	}

	private D _add(D instanceData) {
		instanceData.owner = this;

		instanceData.dirty = true;
		anyToUpdate = true;
		synchronized (data) {
			data.add(instanceData);
		}

		return instanceData;
	}
}
