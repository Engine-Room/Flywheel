package dev.engine_room.flywheel.backend;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.nio.file.Path;
import java.util.Locale;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonPrimitive;

import dev.engine_room.flywheel.backend.compile.LightSmoothness;
import net.fabricmc.loader.api.FabricLoader;

public class FabricBackendConfig implements BackendConfig {

	public static final Path PATH = FabricLoader.getInstance()
			.getConfigDir()
			.resolve("flywheel-backend.json");

	public static final FabricBackendConfig INSTANCE = new FabricBackendConfig(PATH.toFile());

	private static final Gson GSON = new GsonBuilder().setPrettyPrinting()
			.create();

	private final File file;

	public LightSmoothness lightSmoothness = LightSmoothness.SMOOTH;

	public FabricBackendConfig(File file) {
		this.file = file;
	}

	@Override
	public LightSmoothness lightSmoothness() {
		return lightSmoothness;
	}

	public void load() {
		if (file.exists()) {
			try (FileReader reader = new FileReader(file)) {
				fromJson(JsonParser.parseReader(reader));
			} catch (Exception e) {
				FlwBackend.LOGGER.warn("Could not load config from file '{}'", file.getAbsolutePath(), e);
			}
		}
		// In case we found an error in the config file, immediately save to fix it.
		save();
	}

	public void save() {
		try (FileWriter writer = new FileWriter(file)) {
			GSON.toJson(toJson(), writer);
		} catch (Exception e) {
			FlwBackend.LOGGER.warn("Could not save config to file '{}'", file.getAbsolutePath(), e);
		}
	}

	public void fromJson(JsonElement json) {
		if (!(json instanceof JsonObject object)) {
			FlwBackend.LOGGER.warn("Config JSON must be an object");
			lightSmoothness = LightSmoothness.SMOOTH;
			return;
		}

		readLightSmoothness(object);
	}

	private void readLightSmoothness(JsonObject object) {
		var backendJson = object.get("lightSmoothness");
		String msg = null;

		if (backendJson instanceof JsonPrimitive primitive && primitive.isString()) {
			var value = primitive.getAsString();

			for (var item : LightSmoothness.values()) {
				if (item.name()
						.equalsIgnoreCase(value)) {
					lightSmoothness = item;
					return;
				}
			}

			msg = "Unknown 'lightSmoothness' value: " + value;
		} else if (backendJson != null) {
			msg = "'lightSmoothness' value must be a string";
		}

		// Don't log an error if the field is missing.
		if (msg != null) {
			FlwBackend.LOGGER.warn(msg);
		}
		lightSmoothness = LightSmoothness.SMOOTH;
	}

	public JsonObject toJson() {
		JsonObject object = new JsonObject();
		object.addProperty("lightSmoothness", lightSmoothness.toString()
				.toLowerCase(Locale.ROOT));
		return object;
	}
}
