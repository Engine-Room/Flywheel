package com.jozufozu.flywheel.core.shader;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * A class for manipulating a list of {@code #define} directives.
 *
 * <p>Based loosely on code by jellysquid3.
 */
public class ShaderConstants {

	private final Map<String, String> definitions = new HashMap<>();

	public ShaderConstants define(String def) {
		definitions.put(def, "");
		return this;
	}

	public ShaderConstants define(String def, String value) {
		definitions.put(def, value);
		return this;
	}

	public ShaderConstants define(String def, float value) {
		definitions.put(def, Float.toString(value));
		return this;
	}

	public ShaderConstants defineAll(List<String> defines) {
		for (String def : defines) {
			definitions.put(def, "");
		}
		return this;
	}

	public String build() {
		final StringBuilder acc = new StringBuilder();
		writeInto(acc);
		return acc.toString();
	}

	public void writeInto(final StringBuilder acc) {
		for (Map.Entry<String, String> e : definitions.entrySet()) {
			acc.append("#define ")
					.append(e.getKey());
			if (e.getValue().length() > 0) {
				acc.append(' ')
						.append(e.getValue());
			}
			acc.append('\n');
		}
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		ShaderConstants that = (ShaderConstants) o;
		return Objects.equals(definitions, that.definitions);
	}

	@Override
	public int hashCode() {
		return Objects.hash(definitions);
	}
}
