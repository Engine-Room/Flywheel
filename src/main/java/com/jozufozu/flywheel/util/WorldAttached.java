package com.jozufozu.flywheel.util;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;

import javax.annotation.Nonnull;

import net.minecraft.world.IWorld;

public class WorldAttached<T> {

	// weak references to prevent leaking hashmaps when a WorldAttached is GC'd during runtime
	static List<WeakReference<Map<IWorld, ?>>> allMaps = new ArrayList<>();
	private final Map<IWorld, T> attached;
	private final Function<IWorld, T> factory;

	public WorldAttached(Function<IWorld, T> factory) {
		this.factory = factory;
		attached = new HashMap<>();
		allMaps.add(new WeakReference<>(attached));
	}

	public static void invalidateWorld(IWorld world) {
		Iterator<WeakReference<Map<IWorld, ?>>> i = allMaps.iterator();
		while (i.hasNext()) {
			Map<IWorld, ?> map = i.next()
					.get();
			if (map == null) {
				// If the map has been GC'd, remove the weak reference
				i.remove();
			} else {
				// Prevent leaks
				map.remove(world);
			}
		}
	}

	@Nonnull
	public T get(IWorld world) {
		T t = attached.get(world);
		if (t != null) return t;
		T entry = factory.apply(world);
		put(world, entry);
		return entry;
	}

	public void put(IWorld world, T entry) {
		attached.put(world, entry);
	}

	/**
	 * Replaces the entry with a new one from the factory and returns the new entry.
	 */
	@Nonnull
	public T replace(IWorld world) {
		attached.remove(world);

		return get(world);
	}

	/**
	 * Replaces the entry with a new one from the factory and returns the new entry.
	 */
	@Nonnull
	public T replace(IWorld world, Consumer<T> finalizer) {
		T remove = attached.remove(world);

		if (remove != null)
			finalizer.accept(remove);

		return get(world);
	}

	/**
	 * Deletes all entries after calling a function on them.
	 *
	 * @param finalizer Do something with all of the world-value pairs
	 */
	public void empty(BiConsumer<IWorld, T> finalizer) {
		attached.forEach(finalizer);
		attached.clear();
	}

	/**
	 * Deletes all entries after calling a function on them.
	 *
	 * @param finalizer Do something with all of the values
	 */
	public void empty(Consumer<T> finalizer) {
		attached.values()
				.forEach(finalizer);
		attached.clear();
	}
}
