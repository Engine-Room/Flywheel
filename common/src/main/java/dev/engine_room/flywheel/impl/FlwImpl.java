package dev.engine_room.flywheel.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import dev.engine_room.flywheel.api.Flywheel;
import dev.engine_room.flywheel.backend.FlwBackend;
import dev.engine_room.flywheel.impl.compat.CompatMods;
import dev.engine_room.flywheel.impl.compat.EmbeddiumCompat;
import dev.engine_room.flywheel.impl.registry.IdRegistryImpl;
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

		// backend
		FlwBackend.init(FlwConfig.INSTANCE.backendConfig());

		// vanilla
		VanillaVisuals.init();

		// Embeddium Compat
		if (CompatMods.EMBEDDIUM.isLoaded())
			EmbeddiumCompat.init();
	}

	public static void freezeRegistries() {
		IdRegistryImpl.freezeAll();
	}
}
