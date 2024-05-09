package com.jozufozu.flywheel.impl;

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
import com.jozufozu.flywheel.Flywheel;
import com.jozufozu.flywheel.api.backend.Backend;
import com.jozufozu.flywheel.api.backend.BackendManager;

import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.ResourceLocationException;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

public class FabricFlwConfig implements FlwConfig {
	public static final Path PATH = FabricLoader.getInstance()
			.getConfigDir()
			.resolve("flywheel.json");

	public static final FabricFlwConfig INSTANCE = new FabricFlwConfig(PATH.toFile());

	protected static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

	public static final int WORKER_THREADS_DEFAULT = -1;
	public static final int WORKER_THREADS_MAX = Runtime.getRuntime()
			.availableProcessors();
	public static final boolean LIMIT_UPDATES_DEFAULT = true;

	public Backend backend = BackendManager.getDefaultBackend();
	public boolean limitUpdates = LIMIT_UPDATES_DEFAULT;
	public int workerThreads = WORKER_THREADS_DEFAULT;

	private final File file;

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
				Flywheel.LOGGER.error("Could not load config from file '{}'", file.getAbsolutePath(), e);
			}
		}
		// In case we found an error in the config file, immediately save to fix it.
		save();
	}

	public void save() {
		try (FileWriter writer = new FileWriter(file)) {
			GSON.toJson(toJson(), writer);
		} catch (Exception e) {
			Flywheel.LOGGER.error("Could not save config to file '{}'", file.getAbsolutePath(), e);
		}
	}

	public void fromJson(JsonElement json) {
		if (!(json instanceof JsonObject object)) {
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

		String err = null;

		if (backendJson instanceof JsonPrimitive primitive && primitive.isString()) {
			var backendString = primitive.getAsString();
			try {
				this.backend = Backend.REGISTRY.getOrThrow(new ResourceLocation(backendString));
				return;
			} catch (IllegalArgumentException e) {
				err = "backend ID '" + backendString + "' is not registered";
			} catch (ResourceLocationException e) {
				err = "backend '" + backendString + "' is not a valid resource location";
			} catch (Exception e) {
				// Something else went wrong? This should be dead code.
				err = "backend '" + backendString + "' is invalid";
			}
		} else if (backendJson != null) {
			err = "backend must be a string";
		}

		// Don't log an error if the field is missing.
		if (err != null) {
			Flywheel.LOGGER.warn(err);
		}
		backend = BackendManager.getDefaultBackend();
	}

	private void readLimitUpdates(JsonObject object) {
		var limitUpdatesJson = object.get("limitUpdates");
		if (limitUpdatesJson instanceof JsonPrimitive primitive && primitive.isBoolean()) {
			limitUpdates = primitive.getAsBoolean();
			return;
		} else if (limitUpdatesJson != null) {
			Flywheel.LOGGER.warn("limitUpdates must be a boolean");
		}
		limitUpdates = LIMIT_UPDATES_DEFAULT;
	}

	private void readWorkerThreads(JsonObject object) {
		var workerThreadsJson = object.get("workerThreads");

		if (workerThreadsJson instanceof JsonPrimitive primitive && primitive.isNumber()) {
			int configuredValue = primitive.getAsInt();

			int clamped = Mth.clamp(configuredValue, WORKER_THREADS_DEFAULT, WORKER_THREADS_MAX);

			if (clamped != configuredValue) {
				Flywheel.LOGGER.warn("workerThreads value of {} is out of range, clamping to {}", configuredValue, clamped);
			}

			workerThreads = clamped;
			return;
		} else if (workerThreadsJson != null) {
			Flywheel.LOGGER.warn("workerThreads must be an integer");
		}

		workerThreads = WORKER_THREADS_DEFAULT;
	}

	public JsonObject toJson() {
		JsonObject object = new JsonObject();
		object.addProperty("limitUpdates", limitUpdates);
		object.addProperty("workerThreads", workerThreads);
		object.addProperty("backend", Backend.REGISTRY.getIdOrThrow(backend).toString());
		return object;
	}
}
