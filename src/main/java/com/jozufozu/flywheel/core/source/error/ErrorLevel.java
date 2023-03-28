package com.jozufozu.flywheel.core.source.error;

import com.jozufozu.flywheel.util.ConsoleColors;

public enum ErrorLevel {
	WARN(ConsoleColors.YELLOW + "warn"),
	ERROR(ConsoleColors.RED + "error"),
	HINT(ConsoleColors.WHITE_BRIGHT + "hint"),
	NOTE(ConsoleColors.WHITE_BRIGHT + "note"),
	;

	private final String error;

	ErrorLevel(String error) {
		this.error = error;
	}

	@Override
	public String toString() {
		return error;
	}
}
