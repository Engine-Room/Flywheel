package com.jozufozu.flywheel.backend.source.error.lines;

public interface ErrorLine {

	default int neededMargin() {
		return left().length();
	}

	default Divider divider() {
		return Divider.BAR;
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
