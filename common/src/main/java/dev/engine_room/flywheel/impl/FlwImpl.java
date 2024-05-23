package com.jozufozu.flywheel.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jozufozu.flywheel.api.Flywheel;
import com.jozufozu.flywheel.backend.FlwBackend;
import com.jozufozu.flywheel.impl.registry.IdRegistryImpl;
import com.jozufozu.flywheel.impl.registry.RegistryImpl;
import com.jozufozu.flywheel.lib.instance.InstanceTypes;
import com.jozufozu.flywheel.lib.material.CutoutShaders;
import com.jozufozu.flywheel.lib.material.FogShaders;
import com.jozufozu.flywheel.lib.material.StandardMaterialShaders;
import com.jozufozu.flywheel.lib.util.ShadersModHandler;
import com.jozufozu.flywheel.vanilla.VanillaVisuals;

public final class FlwImpl {
	public static final Logger LOGGER = LoggerFactory.getLogger(Flywheel.ID);
	public static final Logger CONFIG_LOGGER = LoggerFactory.getLogger(Flywheel.ID + "/config");

	private FlwImpl() {
	}

	public static void init() {
		// impl
		BackendManagerImpl.init();

		// lib
		ShadersModHandler.init();
		InstanceTypes.init();
		CutoutShaders.init();
		FogShaders.init();
		StandardMaterialShaders.init();

		// backend
		FlwBackend.init();

		// vanilla
		VanillaVisuals.init();
	}

	public static void freezeRegistries() {
		RegistryImpl.freezeAll();
		IdRegistryImpl.freezeAll();
	}
}
