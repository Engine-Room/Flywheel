package dev.engine_room.flywheel.impl;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.nio.file.Path;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonPrimitive;

import dev.engine_room.flywheel.api.backend.Backend;
import dev.engine_room.flywheel.api.backend.BackendManager;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.ResourceLocationException;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

public class FabricFlwConfig implements FlwConfig {
	public static final Path PATH = FabricLoader.getInstance()
			.getConfigDir()
			.resolve("flywheel.json");

	private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

	public static final boolean LIMIT_UPDATES_DEFAULT = true;
	public static final int WORKER_THREADS_DEFAULT = -1;
	public static final int WORKER_THREADS_MAX = Runtime.getRuntime()
			.availableProcessors();

	public static final FabricFlwConfig INSTANCE = new FabricFlwConfig(PATH.toFile());

	private final File file;

	public Backend backend = BackendManager.getDefaultBackend();
	public boolean limitUpdates = LIMIT_UPDATES_DEFAULT;
	public int workerThreads = WORKER_THREADS_DEFAULT;

	public FabricFlwConfig(File file) {
		this.file = file;
	}

	@Override
	public Backend backend() {
		return backend;
	}

	@Override
	public boolean limitUpdates() {
		return limitUpdates;
	}

	@Override
	public int workerThreads() {
		return workerThreads;
	}

	public void load() {
		if (file.exists()) {
			try (FileReader reader = new FileReader(file)) {
				fromJson(JsonParser.parseReader(reader));
			} catch (Exception e) {
				FlwImpl.CONFIG_LOGGER.warn("Could not load config from file '{}'", file.getAbsolutePath(), e);
			}
		}
		// In case we found an error in the config file, immediately save to fix it.
		save();
	}

	public void save() {
		try (FileWriter writer = new FileWriter(file)) {
			GSON.toJson(toJson(), writer);
		} catch (Exception e) {
			FlwImpl.CONFIG_LOGGER.warn("Could not save config to file '{}'", file.getAbsolutePath(), e);
		}
	}

	public void fromJson(JsonElement json) {
		if (!(json instanceof JsonObject object)) {
			FlwImpl.CONFIG_LOGGER.warn("Config JSON must be an object");
			backend = BackendManager.getDefaultBackend();
			limitUpdates = LIMIT_UPDATES_DEFAULT;
			workerThreads = WORKER_THREADS_DEFAULT;
			return;
		}

		readBackend(object);
		readLimitUpdates(object);
		readWorkerThreads(object);
	}

	private void readBackend(JsonObject object) {
		var backendJson = object.get("backend");
		String msg = null;

		if (backendJson instanceof JsonPrimitive primitive && primitive.isString()) {
			var value = primitive.getAsString();
			try {
				this.backend = Backend.REGISTRY.getOrThrow(ResourceLocation.parse(value));
				return;
			} catch (ResourceLocationException e) {
				msg = "'backend' value '" + value + "' is not a valid resource location";
			} catch (IllegalArgumentException e) {
				msg = "Backend with ID '" + value + "' is not registered";
			} catch (Exception e) {
				// Something else went wrong? This should be dead code.
				msg = "'backend' value '" + value + "' is invalid";
			}
		} else if (backendJson != null) {
			msg = "'backend' value must be a string";
		}

		// Don't log an error if the field is missing.
		if (msg != null) {
			FlwImpl.CONFIG_LOGGER.warn(msg);
		}
		backend = BackendManager.getDefaultBackend();
	}

	private void readLimitUpdates(JsonObject object) {
		var limitUpdatesJson = object.get("limitUpdates");

		if (limitUpdatesJson instanceof JsonPrimitive primitive && primitive.isBoolean()) {
			limitUpdates = primitive.getAsBoolean();
			return;
		} else if (limitUpdatesJson != null) {
			FlwImpl.CONFIG_LOGGER.warn("'limitUpdates' value must be a boolean");
		}

		limitUpdates = LIMIT_UPDATES_DEFAULT;
	}

	private void readWorkerThreads(JsonObject object) {
		var workerThreadsJson = object.get("workerThreads");

		if (workerThreadsJson instanceof JsonPrimitive primitive && primitive.isNumber()) {
			int value = primitive.getAsInt();
			int clamped = Mth.clamp(value, WORKER_THREADS_DEFAULT, WORKER_THREADS_MAX);

			if (clamped != value) {
				FlwImpl.CONFIG_LOGGER.warn("'workerThreads' value of {} is out of range, clamping to {}", value, clamped);
			}

			workerThreads = clamped;
			return;
		} else if (workerThreadsJson != null) {
			FlwImpl.CONFIG_LOGGER.warn("'workerThreads' value must be an integer");
		}

		workerThreads = WORKER_THREADS_DEFAULT;
	}

	public JsonObject toJson() {
		JsonObject object = new JsonObject();
		object.addProperty("backend", Backend.REGISTRY.getIdOrThrow(backend).toString());
		object.addProperty("limitUpdates", limitUpdates);
		object.addProperty("workerThreads", workerThreads);
		return object;
	}
}
