package com.jozufozu.flywheel;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.SemanticVersion;
import net.fabricmc.loader.api.Version;
import net.minecraft.resources.ResourceLocation;

public class Flywheel {

	public static final String ID = "flywheel";
	public static final Logger LOGGER = LogManager.getLogger(Flywheel.class);
	public static SemanticVersion VERSION;

	static void initVersion() {
		Version version = FabricLoader.getInstance()
				.getModContainer(ID)
				.orElseThrow(() -> new IllegalStateException("Could not get the mod container for Flywheel!"))
				.getMetadata()
				.getVersion();
		if (!(version instanceof SemanticVersion semver)) {
			throw new IllegalStateException("Got non-semantic version for Flywheel!");
		}
		VERSION = semver;
	}

	public static ResourceLocation rl(String path) {
		return new ResourceLocation(ID, path);
	}
}
