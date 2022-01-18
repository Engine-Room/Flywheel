package com.jozufozu.flywheel.core.compile;

import java.util.HashMap;
import java.util.Map;

public abstract class Memoizer<K, V> {

	private final Map<K, V> map = new HashMap<>();

	public V get(K key) {
		return map.computeIfAbsent(key, this::_create);
	}

	public void invalidate() {
		map.values().forEach(this::_destroy);
		map.clear();
	}

	protected abstract V _create(K key);

	protected abstract void _destroy(V value);
}
