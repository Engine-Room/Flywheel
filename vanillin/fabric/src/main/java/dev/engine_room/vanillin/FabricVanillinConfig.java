package dev.engine_room.vanillin;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.annotations.SerializedName;

import dev.engine_room.vanillin.config.Configurator;
import dev.engine_room.vanillin.config.ModOverrides;
import dev.engine_room.vanillin.config.VisualConfigValue;
import dev.engine_room.vanillin.config.VisualOverride;
import dev.engine_room.vanillin.config.VisualOverrideValue;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.ModContainer;
import net.fabricmc.loader.api.metadata.CustomValue;
import net.fabricmc.loader.api.metadata.ModMetadata;

public class FabricVanillinConfig {
	public static final Path PATH = FabricLoader.getInstance()
			.getConfigDir()
			.resolve("vanillin.json");

	public static final FabricVanillinConfig INSTANCE = new FabricVanillinConfig(PATH.toFile());

	private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
	public static final String VANILLIN_OVERRIDES = "vanillin:overrides";

	private final File file;

	private ModOverrides overrides;
	private Config config = new Config();

	public FabricVanillinConfig(File file) {
		this.file = file;
	}

	public void load() {
		if (file.exists()) {
			try (FileReader reader = new FileReader(file)) {
				config = GSON.fromJson(reader, Config.class);
			} catch (Exception e) {
				Vanillin.CONFIG_LOGGER.warn("Could not load config from file '{}'", file.getAbsolutePath(), e);
			}
		}

		overrides = modOverrides();
	}

	public void apply(Configurator configurator) {
		var blockEntities = config.blockEntities;
		var blockEntityOverrides = this.overrides.blockEntities();

		for (Configurator.ConfiguredVisual configured : configurator.blockEntities.values()) {
			apply(configured, blockEntities, blockEntityOverrides);
		}

		var entities = config.entities;
		var entityOverrides = this.overrides.entities();
		for (Configurator.ConfiguredVisual configured : configurator.entities.values()) {
			apply(configured, entities, entityOverrides);
		}
	}

	private static void apply(Configurator.ConfiguredVisual configured, Map<String, VisualConfigValue> config, Map<String, List<VisualOverride>> overrides) {
		var key = configured.configKey();
		var enabled = config.computeIfAbsent(key, $ -> VisualConfigValue.DEFAULT);

		configured.set(enabled, overrides.get(key));
	}

	public void save() {
		try (FileWriter writer = new FileWriter(file)) {
			GSON.toJson(config, writer);
		} catch (Exception e) {
			Vanillin.CONFIG_LOGGER.warn("Could not save config to file '{}'", file.getAbsolutePath(), e);
		}
	}

	public static ModOverrides modOverrides() {
		var blockEntities = new ArrayList<VisualOverride>();
		var entities = new ArrayList<VisualOverride>();

		for (ModContainer container : FabricLoader.getInstance().getAllMods()) {
			ModMetadata meta = container.getMetadata();
			var modid = meta.getId();

			if (meta.containsCustomValue(VANILLIN_OVERRIDES)) {
				CustomValue overridesValue = meta.getCustomValue(VANILLIN_OVERRIDES);

				if (overridesValue.getType() != CustomValue.CvType.OBJECT) {
					Vanillin.CONFIG_LOGGER.warn("Mod '{}' attempted to override options with an invalid value, ignoring", modid);
					continue;
				}

				var overrides = overridesValue.getAsObject();

				readSection(blockEntities, modid, overrides, "block_entities", "block entity");
				readSection(entities, modid, overrides, "entities", "entity");
			}
		}

		return new ModOverrides(blockEntities, entities);
	}

	private static void readSection(List<VisualOverride> dst, String modid, CustomValue.CvObject overrides, String sectionName, String singular) {
		if (!overrides.containsKey(sectionName)) {
			return;
		}

		var section = overrides.get(sectionName);

		if (section.getType() != CustomValue.CvType.OBJECT) {
			Vanillin.CONFIG_LOGGER.warn("Mod '{}' attempted to override {} with an invalid value, ignoring", modid, sectionName);
			return;
		}

		for (Map.Entry<String, CustomValue> entry : section.getAsObject()) {
			var value = entry.getValue();
			var key = entry.getKey();
			if (value.getType() != CustomValue.CvType.STRING) {
				Vanillin.CONFIG_LOGGER.warn("Mod '{}' attempted to override {} '{}' with an invalid value, ignoring", modid, singular, key);
				continue;
			}

			var valueString = value.getAsString();

			var parsed = VisualOverrideValue.parse(valueString);

			if (parsed == null) {
				Vanillin.CONFIG_LOGGER.warn("Mod '{}' attempted to override {} '{}' with an invalid value '{}', ignoring", modid, singular, key, valueString);
				continue;
			}

			dst.add(new VisualOverride(key, modid, parsed));
		}
	}

	public static class Config {
		@SerializedName("block_entities")
		public Map<String, VisualConfigValue> blockEntities;
		public Map<String, VisualConfigValue> entities;

		public Config() {
			this(new HashMap<>(), new HashMap<>());
		}

		public Config(Map<String, VisualConfigValue> blockEntities, Map<String, VisualConfigValue> entities) {
			this.blockEntities = blockEntities;
			this.entities = entities;
		}
	}
}
