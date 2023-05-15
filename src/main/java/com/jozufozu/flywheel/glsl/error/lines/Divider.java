package com.jozufozu.flywheel.glsl.error.lines;

public enum Divider {
	BAR(" | "),
	ARROW("-> "),
	EQUALS(" = "),
	;

	private final String s;

	Divider(String s) {
		this.s = s;
	}

	@Override
	public String toString() {
		return s;
	}
}
