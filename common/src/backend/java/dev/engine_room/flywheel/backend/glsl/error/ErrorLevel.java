package dev.engine_room.flywheel.backend.glsl.error;

public enum ErrorLevel {
	WARN(ConsoleColors.YELLOW, "warn"),
	ERROR(ConsoleColors.RED, "error"),
	HINT(ConsoleColors.WHITE_BRIGHT, "hint"),
	NOTE(ConsoleColors.WHITE_BRIGHT, "note"),
	;

	private final String color;
	private final String error;

	ErrorLevel(String color, String error) {
		this.color = color;
		this.error = error;
	}

	@Override
	public String toString() {
		if (ErrorBuilder.CONSOLE_COLORS) {
			return color + error;
		} else {
			return error;
		}
	}
}
