package dev.engine_room.flywheel.lib.model;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

import org.jetbrains.annotations.ApiStatus;

import dev.engine_room.flywheel.api.model.Model;
import dev.engine_room.flywheel.lib.util.FlwUtil;

public final class ModelCache<T> {
	private static final Set<ModelCache<?>> ALL = FlwUtil.createWeakHashSet();
	private final Function<T, Model> factory;
	private final Map<T, Model> map = new ConcurrentHashMap<>();

	public ModelCache(Function<T, Model> factory) {
		this.factory = factory;

		synchronized (ALL) {
			ALL.add(this);
		}
	}

	public Model get(T key) {
		return map.computeIfAbsent(key, factory);
	}

	public void clear() {
		map.clear();
	}

	@ApiStatus.Internal
	public static void onEndClientResourceReload() {
		for (ModelCache<?> cache : ALL) {
			cache.clear();
		}
	}
}
