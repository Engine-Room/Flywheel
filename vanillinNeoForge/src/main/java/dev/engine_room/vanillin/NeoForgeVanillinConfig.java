package dev.engine_room.vanillin;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.electronwill.nightconfig.core.Config;

import dev.engine_room.vanillin.config.Configurator;
import dev.engine_room.vanillin.config.ModOverrides;
import dev.engine_room.vanillin.config.VisualConfigValue;
import dev.engine_room.vanillin.config.VisualOverride;
import dev.engine_room.vanillin.config.VisualOverrideValue;
import net.neoforged.fml.ModList;
import net.neoforged.fml.ModLoadingContext;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.neoforge.common.ModConfigSpec;
import net.neoforged.neoforgespi.language.IModInfo;

public class NeoForgeVanillinConfig {
	public static final NeoForgeVanillinConfig INSTANCE = new NeoForgeVanillinConfig(VanillaVisuals.CONFIGURATOR);

	private final Configurator configurator;
	private final ModConfigSpec clientSpec;

	private final ConfigSection blockEntities;
	private final ConfigSection entities;

	private NeoForgeVanillinConfig(Configurator configurator) {
		this.configurator = configurator;
		var builder = new ModConfigSpec.Builder();

		// Seems like we need to register all field ahead of time so this constructor must run after VanillaVisuals#init
		var blockEntities = setup(builder, configurator.blockEntities.values(), "block_entities");
		var entities = setup(builder, configurator.entities.values(), "entities");
		clientSpec = builder.build();

		var modOverrides = modOverrides();

		this.blockEntities = new ConfigSection(blockEntities, modOverrides.blockEntities());
		this.entities = new ConfigSection(entities, modOverrides.entities());
	}

	public void apply() {
		blockEntities.apply(configurator.blockEntities.values());
		entities.apply(configurator.entities.values());
	}

	public void registerSpecs(ModLoadingContext context) {
		context.getActiveContainer().registerConfig(ModConfig.Type.CLIENT, clientSpec);
	}

	private static ModOverrides modOverrides() {
		var blockEntities = new ArrayList<VisualOverride>();
		var entities = new ArrayList<VisualOverride>();

		ModList.get()
				.forEachModFile(file -> {
					var info = file.getModFileInfo();
					for (IModInfo mod : info.getMods()) {
						var modId = mod.getModId();
						var modProperties = mod.getModProperties()
								.get("vanillin:overrides");

						if (modProperties == null) {
							continue;
						}

						// There's no well-defined API for custom properties like in fabric.
						// It just returns an object, but internally it's represented with nightconfig.
						if (modProperties instanceof Config config) {
							readSection(blockEntities, modId, config, "block_entities", "block entity");
							readSection(entities, modId, config, "entities", "entity");
						} else {
							Vanillin.CONFIG_LOGGER.warn("Mod '{}' attempted to override options with an invalid value, ignoring", modId);
						}
					}
				});

		return new ModOverrides(blockEntities, entities);
	}

	private static void readSection(List<VisualOverride> dst, String modId, Config config, String section, String singular) {
		if (!config.contains(section)) {
			return;
		}

		var sectionObject = config.getRaw(section);

		if (sectionObject instanceof Config sectionConfig) {
			for (var entry : sectionConfig.entrySet()) {
				var key = entry.getKey();
				var value = entry.getValue();

				if (value instanceof String valueString) {
					var parsed = VisualOverrideValue.parse(valueString);

					if (parsed != null) {
						dst.add(new VisualOverride(key, modId, parsed));
					} else {
						Vanillin.CONFIG_LOGGER.warn("Mod '{}' attempted to override {} '{}' with an invalid value '{}', ignoring", modId, singular, key, valueString);
					}
				} else {
					Vanillin.CONFIG_LOGGER.warn("Mod '{}' attempted to override {} '{}' with an invalid value, ignoring", modId, singular, key);
				}
			}
		} else {
			Vanillin.CONFIG_LOGGER.warn("Mod '{}' attempted to override {} with an invalid value, ignoring", modId, section);
		}
	}

	private static Map<String, ModConfigSpec.EnumValue<VisualConfigValue>> setup(ModConfigSpec.Builder builder, Collection<? extends Configurator.ConfiguredVisual> configuredVisuals, String push) {
		var out = new HashMap<String, ModConfigSpec.EnumValue<VisualConfigValue>>();
		builder.push(push);

		for (var configured : configuredVisuals) {
			var name = configured.configKey();
			var config = builder.defineEnum(name, VisualConfigValue.DEFAULT);
			out.put(name, config);
		}

		builder.pop();

		return out;
	}

	private record ConfigSection(Map<String, ModConfigSpec.EnumValue<VisualConfigValue>> config, Map<String, List<VisualOverride>> overrides) {
		void apply(Collection<? extends Configurator.ConfiguredVisual> values) {
			for (var configured : values) {
				var key = configured.configKey();
				var value = config.get(key);
				if (value != null) {
					configured.set(value.get(), overrides.get(key));
				}
			}
		}
	}
}
