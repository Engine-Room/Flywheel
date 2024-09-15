package dev.engine_room.flywheel.lib.model.part;

import java.util.function.Function;

import dev.engine_room.flywheel.lib.model.ResourceReloadCache;

/**
 * If the lookup to create your model tree directly through {@link ModelTree#of} is particularly expensive,
 * you can memoize the arguments here to hide the cost.
 */
public class ModelTreeCache<T> extends ResourceReloadCache<T, ModelTree> {
	public ModelTreeCache(Function<T, ModelTree> factory) {
		super(factory);
	}
}
