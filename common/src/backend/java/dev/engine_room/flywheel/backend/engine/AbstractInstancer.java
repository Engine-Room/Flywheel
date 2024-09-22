package dev.engine_room.flywheel.backend.engine;

import java.util.ArrayList;
import java.util.function.Supplier;

import org.jetbrains.annotations.Nullable;

import dev.engine_room.flywheel.api.instance.Instance;
import dev.engine_room.flywheel.api.instance.InstanceType;
import dev.engine_room.flywheel.api.instance.Instancer;
import dev.engine_room.flywheel.backend.engine.embed.Environment;
import dev.engine_room.flywheel.backend.util.AtomicBitSet;

public abstract class AbstractInstancer<I extends Instance> implements Instancer<I>, InstanceHandleImpl.State<I> {
	public final InstanceType<I> type;
	public final Environment environment;
	private final InstanceHandleImpl.Hidden<I> hidden;

	// Lock for all instances, only needs to be used in methods that may run on the TaskExecutor.
	protected final Object lock = new Object();
	protected final ArrayList<I> instances = new ArrayList<>();
	protected final ArrayList<InstanceHandleImpl<I>> handles = new ArrayList<>();

	protected final AtomicBitSet changed = new AtomicBitSet();
	protected final AtomicBitSet deleted = new AtomicBitSet();

	protected AbstractInstancer(InstancerKey<I> key, Supplier<AbstractInstancer<I>> recreate) {
		this.type = key.type();
		this.environment = key.environment();
		this.hidden = new InstanceHandleImpl.Hidden<>(recreate);
	}

	@Override
	public InstanceHandleImpl.State<I> setChanged(int index) {
		notifyDirty(index);
		return this;
	}

	@Override
	public InstanceHandleImpl.State<I> setDeleted(int index) {
		notifyRemoval(index);
		return InstanceHandleImpl.Deleted.instance();
	}

	@Override
	public InstanceHandleImpl.State<I> setVisible(int index, I instance, boolean visible) {
		if (visible) {
			return this;
		}

		notifyRemoval(index);

		return hidden;
	}

	@Override
	public InstanceHandleImpl.Status status() {
		return InstanceHandleImpl.Status.VISIBLE;
	}

	@Override
	public I createInstance() {
		var handle = new InstanceHandleImpl<>(this);
		I instance = type.create(handle);
		handle.instance = instance;

		synchronized (lock) {
			handle.index = instances.size();
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

		if (!(instanceHandle instanceof InstanceHandleImpl<?>)) {
			// UB: do nothing
			return;
		}

		// Should InstanceType have an isInstance method?
		var handle = (InstanceHandleImpl<I>) instanceHandle;

		// Should you be allowed to steal deleted instances?
		if (handle.state == this || handle.state.status() == InstanceHandleImpl.Status.DELETED) {
			return;
		}

		// FIXME: in theory there could be a race condition here if the instance
		//  is somehow being stolen by 2 different instancers between threads.
		//  That seems kinda impossible so I'm fine leaving it as is for now.

		// Remove the instance from its old instancer.
		// This won't have any unwanted effect when the old instancer
		// is filtering deleted instances later, so is safe.
		handle.setDeleted();

		// Add the instance to this instancer.
		switch (handle.state.status()) {
		case VISIBLE:
			handle.state = this;
			// Only lock now that we'll be mutating our state.
			synchronized (lock) {
				handle.index = instances.size();
				addLocked(instance, handle);
			}
			break;
		case HIDDEN:
			handle.state = hidden;
			break;
		}
	}

	/**
	 * Calls must be synchronized on {@link #lock}.
	 */
	private void addLocked(I instance, InstanceHandleImpl<I> handle) {
		instances.add(instance);
		handles.add(handle);
		setIndexChanged(handle.index);
	}

	public int instanceCount() {
		return instances.size();
	}

	public void notifyDirty(int index) {
		if (index < 0 || index >= instanceCount()) {
			return;
		}
		setIndexChanged(index);
	}

	protected void setIndexChanged(int index) {
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
			setRangeChanged(writePos, newSize);
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

	protected void setRangeChanged(int start, int end) {
		changed.set(start, end);
	}

	/**
	 * Clear all instances without freeing resources.
	 */
	public void clear() {
		for (InstanceHandleImpl<I> handle : handles) {
			// Only clear instances that belong to this instancer.
			// If one of these handles was stolen by another instancer,
			// clearing it here would cause significant visual artifacts and instance leaks.
			// At the same time, we need to clear handles we own to prevent
			// instances from changing/deleting positions in this instancer that no longer exist.
			if (handle.state == this || handle.state == hidden) {
				handle.clear();
				handle.state = InstanceHandleImpl.Deleted.instance();
			}
		}
		instances.clear();
		handles.clear();
		changed.clear();
		deleted.clear();
	}

	public abstract void delete();

	@Override
	public String toString() {
		return "AbstractInstancer[" + instanceCount() + ']';
	}
}
