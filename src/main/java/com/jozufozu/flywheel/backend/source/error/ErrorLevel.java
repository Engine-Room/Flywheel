package com.jozufozu.flywheel.backend.source.error;

public enum ErrorLevel {
	WARN("warn"),
	ERROR("error"),
	HINT("hint"),
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
