package com.jozufozu.flywheel.lib.visual;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

import com.jozufozu.flywheel.api.instance.Instance;

public class SmartRecycler<K, I extends Instance> {
	private final Function<K, I> factory;
	private final Map<K, InstanceRecycler<I>> recyclers = new HashMap<>();

	public SmartRecycler(Function<K, I> factory) {
		this.factory = factory;
	}

	public void resetCount() {
		recyclers.values()
				.forEach(InstanceRecycler::resetCount);
	}

	public I get(K key) {
		return recyclers.computeIfAbsent(key, k -> new InstanceRecycler<>(() -> factory.apply(k)))
				.get();
	}

	public void discardExtra() {
		recyclers.values()
				.forEach(InstanceRecycler::discardExtra);
	}

	public void delete() {
		recyclers.values()
				.forEach(InstanceRecycler::delete);
		recyclers.clear();
	}
}
