package dev.engine_room.flywheel.lib.util;

import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

import org.jetbrains.annotations.ApiStatus;

public final class ResourceReloadCache<T, U> implements Function<T, U> {
	private static final Set<ResourceReloadCache<?, ?>> ALL = Collections.newSetFromMap(new WeakHashMap<>());
	private final Function<T, U> factory;
	private final Map<T, U> map = new ConcurrentHashMap<>();

	public ResourceReloadCache(Function<T, U> factory) {
		this.factory = factory;

		synchronized (ALL) {
			ALL.add(this);
		}
	}

	public final U get(T key) {
		return map.computeIfAbsent(key, factory);
	}

	@Override
	public final U apply(T t) {
		return get(t);
	}

	public final void clear() {
		map.clear();
	}

	@ApiStatus.Internal
	public static void onEndClientResourceReload() {
		for (ResourceReloadCache<?, ?> cache : ALL) {
			cache.clear();
		}
	}
}
