package dev.engine_room.flywheel.backend.engine;

import java.util.ArrayList;

import org.jetbrains.annotations.Nullable;

import dev.engine_room.flywheel.api.instance.Instance;
import dev.engine_room.flywheel.backend.util.AtomicBitSet;

public abstract class BaseInstancer<I extends Instance> extends AbstractInstancer<I> implements InstanceHandleImpl.State<I> {
	// Lock for all instances, only needs to be used in methods that may run on the TaskExecutor.
	protected final Object lock = new Object();
	protected final ArrayList<I> instances = new ArrayList<>();
	protected final ArrayList<InstanceHandleImpl<I>> handles = new ArrayList<>();

	protected final AtomicBitSet changed = new AtomicBitSet();
	protected final AtomicBitSet deleted = new AtomicBitSet();

	protected BaseInstancer(InstancerKey<I> key, Recreate<I> recreate) {
		super(key, recreate);
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
	public InstanceHandleImpl.State<I> setVisible(InstanceHandleImpl<I> handle, int index, boolean visible) {
		if (visible) {
			return this;
		}

		notifyRemoval(index);

		I instance;
		synchronized (lock) {
			// I think we need to lock to prevent wacky stuff from happening if the array gets resized.
			instance = instances.get(index);
		}

		return new InstanceHandleImpl.Hidden<>(recreate, instance);
	}

	@Override
	public I createInstance() {
		var handle = new InstanceHandleImpl<>(this);
		I instance = type.create(handle);

		synchronized (lock) {
			handle.index = instances.size();
			addLocked(instance, handle);
			return instance;
		}
	}

	public InstanceHandleImpl.State<I> revealInstance(InstanceHandleImpl<I> handle, I instance) {
		synchronized (lock) {
			handle.index = instances.size();
			addLocked(instance, handle);
		}
		return this;
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
		@SuppressWarnings("unchecked") var handle = (InstanceHandleImpl<I>) instanceHandle;

		// No need to steal if this instance is already owned by this instancer.
		if (handle.state == this) {
			return;
		}
		// Not allowed to steal deleted instances.
		if (handle.state instanceof InstanceHandleImpl.Deleted) {
			return;
		}
		// No need to steal if the instance will recreate to us.
		if (handle.state instanceof InstanceHandleImpl.Hidden<I> hidden && recreate.equals(hidden.recreate())) {
			return;
		}

		// FIXME: in theory there could be a race condition here if the instance
		//  is somehow being stolen by 2 different instancers between threads.
		//  That seems kinda impossible so I'm fine leaving it as is for now.

		// Add the instance to this instancer.
		if (handle.state instanceof BaseInstancer<I> other) {
			// Remove the instance from its old instancer.
			// This won't have any unwanted effect when the old instancer
			// is filtering deleted instances later, so is safe.
			other.notifyRemoval(handle.index);

			handle.state = this;
			// Only lock now that we'll be mutating our state.
			synchronized (lock) {
				handle.index = instances.size();
				addLocked(instance, handle);
			}
		} else if (handle.state instanceof InstanceHandleImpl.Hidden<I>) {
			handle.state = new InstanceHandleImpl.Hidden<>(recreate, instance);
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
			if (handle.state == this) {
				handle.clear();
				handle.state = InstanceHandleImpl.Deleted.instance();
			}
		}
		instances.clear();
		handles.clear();
		changed.clear();
		deleted.clear();
	}
}
