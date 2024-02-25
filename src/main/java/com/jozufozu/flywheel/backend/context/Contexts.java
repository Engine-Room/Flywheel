package com.jozufozu.flywheel.backend.context;

public final class Contexts {
	public static final Context DEFAULT = SimpleContext.builder(ContextShaders.DEFAULT)
			.build();

	private Contexts() {
	}
}
