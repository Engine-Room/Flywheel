package com.jozufozu.flywheel.lib.visual;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

import com.jozufozu.flywheel.api.instance.Instance;

/**
 * A utility for recycling instances.
 * <br>
 * If the exact number of instances you need each frame is unknown, you can use this to manage
 * a pool of instances that will be dynamically created, deleted, or re-used as necessary.
 *
 * @param <I> The type of instance to recycle.
 */
public class InstanceRecycler<I extends Instance> {

	private final Supplier<I> factory;

	private final List<I> instances = new ArrayList<>();

	private int count;

	public InstanceRecycler(Supplier<I> factory) {
		this.factory = factory;
	}

	/**
	 * Reset the count of instances.
	 * <br>
	 * Call this at before your first call to {@link #get()} each frame.
	 */
	public void resetCount() {
		count = 0;
	}

	/**
	 * Get the next instance in the pool, creating a new one if necessary.
	 * <br>
	 * The returned instance may not be in its default state.
	 *
	 * @return The next instance in the pool.
	 */
	public I get() {
		var lastCount = count++;
		if (lastCount < instances.size()) {
			return instances.get(lastCount);
		} else {
			var out = factory.get();
			instances.add(out);
			return out;
		}
	}

	/**
	 * Delete any instances that were not used this frame.
	 * <br>
	 * Call this after your last call to {@link #get()} each frame.
	 */
	public void discardExtra() {
		for (int i = count; i < instances.size(); i++) {
			instances.get(i)
					.delete();
		}
		instances.subList(count, instances.size())
				.clear();
	}

	public void delete() {
		instances.forEach(Instance::delete);
		instances.clear();
	}
}
