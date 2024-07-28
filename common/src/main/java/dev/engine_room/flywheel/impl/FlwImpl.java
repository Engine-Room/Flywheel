package dev.engine_room.flywheel.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import dev.engine_room.flywheel.api.Flywheel;
import dev.engine_room.flywheel.backend.FlwBackend;
import dev.engine_room.flywheel.impl.registry.IdRegistryImpl;
import dev.engine_room.flywheel.impl.registry.RegistryImpl;
import dev.engine_room.flywheel.lib.instance.InstanceTypes;
import dev.engine_room.flywheel.lib.material.CutoutShaders;
import dev.engine_room.flywheel.lib.material.FogShaders;
import dev.engine_room.flywheel.lib.material.LightShaders;
import dev.engine_room.flywheel.lib.material.StandardMaterialShaders;
import dev.engine_room.flywheel.lib.util.ShadersModHandler;
import dev.engine_room.flywheel.vanilla.VanillaVisuals;

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
		LightShaders.init();
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
