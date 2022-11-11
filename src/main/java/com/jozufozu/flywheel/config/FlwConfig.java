package com.jozufozu.flywheel.config;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.Collections;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.Nullable;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;
import com.jozufozu.flywheel.config.Option.BooleanOption;
import com.jozufozu.flywheel.config.Option.EnumOption;

import it.unimi.dsi.fastutil.objects.Object2ObjectLinkedOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectMap;
import it.unimi.dsi.fastutil.objects.ObjectBidirectionalIterator;
import net.fabricmc.loader.api.FabricLoader;

public class FlwConfig {
	protected static final Logger LOGGER = LogManager.getLogger("Flywheel Config");
	protected static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

	private static final FlwConfig INSTANCE = new FlwConfig(FabricLoader.getInstance().getConfigDir().resolve("flywheel.json").toFile());

	protected final File file;
	protected final Object2ObjectLinkedOpenHashMap<String, Option<?>> optionMap = new Object2ObjectLinkedOpenHashMap<>();
	protected final Map<String, Option<?>> optionMapView = Collections.unmodifiableMap(optionMap);

	/** Select the backend to use. */
	public final EnumOption<BackendType> backend = addOption(new EnumOption<>("backend", BackendType.INSTANCING));
	/** Enable or disable a debug overlay that colors pixels by their normal. */
	public final BooleanOption debugNormals = addOption(new BooleanOption("debugNormals", false));
	/** Enable or disable instance update limiting with distance. */
	public final BooleanOption limitUpdates = addOption(new BooleanOption("limitUpdates", true));

	public FlwConfig(File file) {
		this.file = file;
	}

	public static FlwConfig get() {
		return INSTANCE;
	}

	public static void init() {
		INSTANCE.load();
	}

	public BackendType getBackendType() {
		return backend.get();
	}

	public boolean debugNormals() {
		return debugNormals.get();
	}

	public boolean limitUpdates() {
		return limitUpdates.get();
	}

	public void load() {
		if (file.exists()) {
			try (FileReader reader = new FileReader(file)) {
				fromJson(JsonParser.parseReader(reader));
			} catch (Exception e) {
				LOGGER.error("Could not load config from file '" + file.getAbsolutePath() + "'", e);
			}
		}
		save();
	}

	public void save() {
		try (FileWriter writer = new FileWriter(file)) {
			GSON.toJson(toJson(), writer);
		} catch (Exception e) {
			LOGGER.error("Could not save config to file '" + file.getAbsolutePath() + "'", e);
		}
	}

	protected void fromJson(JsonElement json) throws JsonParseException {
		if (json.isJsonObject()) {
			JsonObject object = json.getAsJsonObject();
			ObjectBidirectionalIterator<Object2ObjectMap.Entry<String, Option<?>>> iterator = optionMap.object2ObjectEntrySet().fastIterator();
			while (iterator.hasNext()) {
				Object2ObjectMap.Entry<String, Option<?>> entry = iterator.next();
				JsonElement element = object.get(entry.getKey());
				if (element != null) {
					entry.getValue().fromJson(element);
				}
			}
		} else {
			throw new JsonParseException("Json must be an object");
		}
	}

	protected JsonElement toJson() {
		JsonObject object = new JsonObject();
		ObjectBidirectionalIterator<Object2ObjectMap.Entry<String, Option<?>>> iterator = optionMap.object2ObjectEntrySet().fastIterator();
		while (iterator.hasNext()) {
			Object2ObjectMap.Entry<String, Option<?>> entry = iterator.next();
			object.add(entry.getKey(), entry.getValue().toJson());
		}
		return object;
	}

	protected <T extends Option<?>> T addOption(T option) {
		Option<?> old = optionMap.put(option.getKey(), option);
		if (old != null) {
			LOGGER.warn("Option with key '" + old.getKey() + "' was overridden");
		}
		return option;
	}

	@Nullable
	public Option<?> getOption(String key) {
		return optionMap.get(key);
	}

	public Map<String, Option<?>> getOptionMapView() {
		return optionMapView;
	}
}
