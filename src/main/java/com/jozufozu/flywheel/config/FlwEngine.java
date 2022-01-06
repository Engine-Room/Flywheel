package com.jozufozu.flywheel.config;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import javax.annotation.Nullable;

public enum FlwEngine {
	OFF("off", "Off"),
	BATCHING("batching", "Parallel Batching"),
	INSTANCING("instancing", "GL33 Instanced Arrays"),
	;

	private static final Map<String, FlwEngine> lookup;

	static {
		lookup = new HashMap<>();
		for (FlwEngine value : values()) {
			lookup.put(value.shortName, value);
		}
	}

	private final String shortName;
	private final String properName;

	FlwEngine(String shortName, String properName) {
		this.shortName = shortName;
		this.properName = properName;
	}

	public String getShortName() {
		return shortName;
	}

	public String getProperName() {
		return properName;
	}

	@Nullable
	public static FlwEngine byName(String name) {
		return lookup.get(name);
	}

	public static Collection<String> validNames() {
		return lookup.keySet();
	}
}
