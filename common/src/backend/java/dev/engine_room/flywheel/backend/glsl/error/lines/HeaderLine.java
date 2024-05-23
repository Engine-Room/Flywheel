package dev.engine_room.flywheel.backend.glsl.error.lines;

public record HeaderLine(String level, CharSequence message) implements ErrorLine {

	@Override
	public int neededMargin() {
		return -1;
	}

	@Override
	public String build() {
		return level + ": " + message;
	}
}
