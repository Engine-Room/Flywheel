package dev.engine_room.flywheel.lib.model;

import java.util.function.Function;

import dev.engine_room.flywheel.api.model.Model;

public final class ModelCache<T> extends ResourceReloadCache<T, Model> {
	public ModelCache(Function<T, Model> factory) {
		super(factory);
	}
}
