package dev.engine_room.flywheel.lib.model;

import java.util.function.Supplier;

import dev.engine_room.flywheel.api.model.Model;

public final class ModelHolder extends ResourceReloadHolder<Model> {
	public ModelHolder(Supplier<Model> factory) {
		super(factory);
	}
}
