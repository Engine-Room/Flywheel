package com.jozufozu.flywheel.config;

import java.util.Collection;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import org.jetbrains.annotations.Nullable;

public enum BackendType {
	OFF("Off"),

	/**
	 * Use a thread pool to buffer instances in parallel on the CPU.
	 */
	BATCHING("Parallel Batching"),

	/**
	 * Use GPU instancing to render everything.
	 */
	INSTANCING("GL33 Instanced Arrays"),

	/**
	 * Use Compute shaders to cull instances.
	 */
	INDIRECT("GL46 Compute Culling"),
	;

	private static final Map<String, BackendType> lookup;

	static {
		lookup = new HashMap<>();
		for (BackendType value : values()) {
			lookup.put(value.getShortName(), value);
		}
	}

	private final String properName;
	private final String shortName;

	BackendType(String properName) {
		this.properName = properName;
		shortName = name().toLowerCase(Locale.ROOT);
	}

	public String getProperName() {
		return properName;
	}

	public String getShortName() {
		return shortName;
	}

	@Nullable
	public static BackendType byName(String name) {
		return lookup.get(name);
	}

	public static Collection<String> validNames() {
		return lookup.keySet();
	}
}
