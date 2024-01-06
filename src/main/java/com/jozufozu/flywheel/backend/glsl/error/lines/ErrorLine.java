package com.jozufozu.flywheel.backend.glsl.error.lines;

public interface ErrorLine {

	default int neededMargin() {
		return left().length();
	}

	default com.jozufozu.flywheel.glsl.error.lines.Divider divider() {
		return com.jozufozu.flywheel.glsl.error.lines.Divider.BAR;
	}

	default String build() {
		return left() + divider() + right();
	}

	default String left() {
		return "";
	}
	default String right() {
		return "";
	}
}
