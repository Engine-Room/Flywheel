package com.jozufozu.flywheel.backend;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jozufozu.flywheel.api.Flywheel;

public final class FlwBackend {
	public static final Logger LOGGER = LoggerFactory.getLogger(Flywheel.ID + "/backend");

	private FlwBackend() {
	}

	public static void init() {
		ShaderIndices.init();
		Backends.init();
	}
}
