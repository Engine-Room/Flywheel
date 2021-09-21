package com.jozufozu.flywheel.backend.source.error;

public enum Level {
	WARN("warn"),
	ERROR("error"),
	;

	private final String error;

	Level(String error) {
		this.error = error;
	}

	@Override
	public String toString() {
		return error;
	}
}
