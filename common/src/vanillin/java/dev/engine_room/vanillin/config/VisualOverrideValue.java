package dev.engine_room.vanillin.config;

import org.jetbrains.annotations.Nullable;

public enum VisualOverrideValue {
	DEFAULT,
	DISABLE,
	;

	@Nullable
	public static VisualOverrideValue parse(String string) {
		if (string.equals("default")) {
			return DEFAULT;
		} else if (string.equals("disable")) {
			return DISABLE;
		}
		return null;
	}
}
