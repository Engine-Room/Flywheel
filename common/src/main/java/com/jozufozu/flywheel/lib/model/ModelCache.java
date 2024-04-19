package com.jozufozu.flywheel.lib.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

import org.jetbrains.annotations.ApiStatus;

import com.jozufozu.flywheel.api.model.Model;

public class ModelCache<T> {
	private static final List<ModelCache<?>> ALL = new ArrayList<>();
	private final Function<T, Model> factory;
	private final Map<T, Model> map = new ConcurrentHashMap<>();

	public ModelCache(Function<T, Model> factory) {
		this.factory = factory;
		ALL.add(this);
	}

	public Model get(T key) {
		return map.computeIfAbsent(key, factory);
	}

	public void clear() {
		map.values().forEach(Model::delete);
		map.clear();
	}

	@ApiStatus.Internal
	public static void onEndClientResourceReload() {
		for (ModelCache<?> cache : ALL) {
			cache.clear();
		}
	}
}
