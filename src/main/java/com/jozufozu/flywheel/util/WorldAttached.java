package com.jozufozu.flywheel.util;

import java.util.HashMap;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;

import net.minecraft.world.level.LevelAccessor;

import org.jetbrains.annotations.NotNull;

public class WorldAttached<T> {

	private final Map<LevelAccessor, T> attached;
	private final Function<LevelAccessor, T> factory;

	public WorldAttached(Function<LevelAccessor, T> factory) {
		this.factory = factory;
		attached = new HashMap<>();
	}

	@NotNull
	public T get(LevelAccessor world) {
		T t = attached.get(world);
		if (t != null) return t;
		T entry = factory.apply(world);
		put(world, entry);
		return entry;
	}

	public void put(LevelAccessor world, T entry) {
		attached.put(world, entry);
	}

	/**
	 * Replaces the entry with a new one from the factory and returns the new entry.
	 */
	@NotNull
	public T replace(LevelAccessor world) {
		attached.remove(world);

		return get(world);
	}

	/**
	 * Replaces the entry with a new one from the factory and returns the new entry.
	 */
	@NotNull
	public T replace(LevelAccessor world, Consumer<T> finalizer) {
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
	public void empty(BiConsumer<LevelAccessor, T> finalizer) {
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
