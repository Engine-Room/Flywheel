package dev.engine_room.vanillin;

import java.util.HashMap;
import java.util.Map;

import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.config.ModConfig;

public class ForgeVanillinConfig {
	public static final ForgeVanillinConfig INSTANCE = new ForgeVanillinConfig(VanillaVisuals.CONFIGURATOR);

	public final Map<String, ForgeConfigSpec.BooleanValue> blockEntities = new HashMap<>();
	public final Map<String, ForgeConfigSpec.BooleanValue> entities = new HashMap<>();

	private final Configurator configurator;
	private final ForgeConfigSpec clientSpec;

	private ForgeVanillinConfig(Configurator configurator) {
		this.configurator = configurator;
		var builder = new ForgeConfigSpec.Builder();

		builder.push("block_entities");

		// Seems like we need to register all field ahead of time so this constructor must run after VanillaVisuals#init
		for (var configured : configurator.blockEntities.values()) {
			var name = configured.configKey();
			var config = builder.define(name, configured.enabledByDefault());
			blockEntities.put(name, config);
		}

		builder.pop();
		builder.push("entities");

		for (var configured : configurator.entities.values()) {
			var name = configured.configKey();
			var config = builder.define(name, configured.defaultEnabled());
			entities.put(name, config);
		}
		clientSpec = builder.build();
	}

	public void apply() {
		for (var configured : configurator.blockEntities.values()) {
			var value = blockEntities.get(configured.configKey());
			if (value != null) {
				configured.set(value.get());
			}
		}

		for (var configured : configurator.entities.values()) {
			var value = entities.get(configured.configKey());
			if (value != null) {
				configured.set(value.get());
			}
		}
	}

	public void registerSpecs(ModLoadingContext context) {
		context.registerConfig(ModConfig.Type.CLIENT, clientSpec);
	}
}
