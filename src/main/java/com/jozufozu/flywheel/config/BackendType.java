package com.jozufozu.flywheel.config;

import java.util.Collection;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import javax.annotation.Nullable;

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
	;

	private static final Map<String, BackendType> lookup;

	static {
		lookup = new HashMap<>();
		for (BackendType value : values()) {
			lookup.put(value.name().toLowerCase(Locale.ROOT), value);
		}
	}

	private final String properName;

	BackendType(String properName) {
		this.properName = properName;
	}

	public String getProperName() {
		return properName;
	}

	public String getShortName() {
		return name().toLowerCase(Locale.ROOT);
	}

	@Nullable
	public static BackendType byName(String name) {
		return lookup.get(name);
	}

	public static Collection<String> validNames() {
		return lookup.keySet();
	}
}
