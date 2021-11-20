package com.jozufozu.flywheel.backend.source.error.lines;

public record HeaderLine(String level, CharSequence message) implements ErrorLine {

	@Override
	public int neededMargin() {
		return 0;
	}

	@Override
	public String build() {
		return level + ": " + message;
	}

	@Override
	public String left() {
		return null;
	}

	@Override
	public String right() {
		return null;
	}
}
