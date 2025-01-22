package dev.engine_room.vanillin;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.annotations.SerializedName;

import net.fabricmc.loader.api.FabricLoader;

public class FabricVanillinConfig {
	public static final Path PATH = FabricLoader.getInstance()
			.getConfigDir()
			.resolve("vanillin.json");

	public static final FabricVanillinConfig INSTANCE = new FabricVanillinConfig(PATH.toFile());

	private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

	private final File file;

	private Config config;

	public FabricVanillinConfig(File file) {
		this.file = file;
	}

	public void load() {
		if (file.exists()) {
			try (FileReader reader = new FileReader(file)) {
				config = GSON.fromJson(reader, Config.class);
			} catch (Exception e) {
				Vanillin.CONFIG_LOGGER.warn("Could not load config from file '{}'", file.getAbsolutePath(), e);
				config = new Config();
			}
		}
	}

	public void apply(Configurator configurator) {
		for (Configurator.ConfiguredBlockEntity<?> configuredBlockEntity : configurator.blockEntities.values()) {
			boolean enabled = config.blockEntities.computeIfAbsent(configuredBlockEntity.configKey(), $ -> configuredBlockEntity.enabledByDefault());
			configuredBlockEntity.set(enabled);
		}

		for (Configurator.ConfiguredEntity<?> configured : configurator.entities.values()) {
			boolean enabled = config.entities.computeIfAbsent(configured.configKey(), $ -> configured.defaultEnabled());
			configured.set(enabled);
		}
	}

	public void save() {
		try (FileWriter writer = new FileWriter(file)) {
			GSON.toJson(config, writer);
		} catch (Exception e) {
			Vanillin.CONFIG_LOGGER.warn("Could not save config to file '{}'", file.getAbsolutePath(), e);
		}
	}

	public static class Config {
		@SerializedName("block_entities")
		public Map<String, Boolean> blockEntities;
		public Map<String, Boolean> entities;

		public Config() {
			this(new HashMap<>(), new HashMap<>());
		}

		public Config(Map<String, Boolean> blockEntities, Map<String, Boolean> entities) {
			this.blockEntities = blockEntities;
			this.entities = entities;
		}
	}
}
