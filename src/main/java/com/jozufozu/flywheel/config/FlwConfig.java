package com.jozufozu.flywheel.config;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;
import com.jozufozu.flywheel.config.Option.BooleanOption;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;

import net.fabricmc.fabric.api.client.command.v1.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v1.FabricClientCommandSource;
import net.fabricmc.loader.api.FabricLoader;

public class FlwConfig {
	private static final FlwConfig INSTANCE = new FlwConfig();
	private static final Logger LOGGER = LogManager.getLogger("Flywheel Config");
	public static final File FILE = FabricLoader.getInstance().getConfigDir().resolve("flywheel.json").toFile();

	private static final JsonParser PARSER = new JsonParser();
	private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

	protected final List<Option<?>> options = new ArrayList<>();

	public final BooleanOption enabled = add(OptionCreation.enabled());
	public final BooleanOption normalOverlay = add(OptionCreation.normalOverlay());
	public final BooleanOption chunkCaching = add(OptionCreation.chunkCaching());

	public static FlwConfig get() {
		return INSTANCE;
	}

	public static void load() {
		get().loadFrom(FILE);
	}

	public static void save() {
		get().saveTo(FILE);
	}

	public static void init() {
		load();

		LiteralArgumentBuilder<FabricClientCommandSource> command = ClientCommandManager.literal("flywheel");
		for (Option<?> option : get().options) {
			LiteralArgumentBuilder<FabricClientCommandSource> builder = ClientCommandManager.literal(option.getId());
			option.buildCommand(builder);
			command.then(builder);
		}
		ClientCommandManager.DISPATCHER.register(command);
	}

	public void loadFrom(File file) {
		if (file.exists()) {
			try (FileReader reader = new FileReader(file)) {
				fromJson(PARSER.parse(reader));
			} catch (IOException e) {
				LOGGER.error("Error loading config from file \"" + file.getAbsolutePath() + "\".", e);
			}
		} else {
			saveTo(file);
		}
	}

	public void saveTo(File file) {
		try (FileWriter writer = new FileWriter(file)) {
			GSON.toJson(toJson(), writer);
		} catch (IOException e) {
			LOGGER.error("Error saving config to file \"" + file.getAbsolutePath() + "\".", e);
		}
	}

	protected void fromJson(JsonElement json) throws IOException {
		if (json.isJsonObject()) {
			JsonObject object = json.getAsJsonObject();
			for (Option<?> option : options) {
				option.fromJson(object.get(option.getId()));
			}
		} else {
			throw new JsonParseException("Json must be of type JsonObject!");
		}
	}

	protected JsonElement toJson() throws IOException {
		JsonObject object = new JsonObject();
		for (Option<?> option : options) {
			object.add(option.getId(), option.toJson());
		}
		return object;
	}

	protected <O, T extends Option<O>> T add(T option) {
		options.add(option);
		return option;
	}

	public boolean enabled() {
		return enabled.get();
	}

	public boolean debugNormals() {
		return normalOverlay.get();
	}

	public boolean chunkCaching() {
		return chunkCaching.get();
	}
}
