package com.jozufozu.flywheel.glsl.error.lines;

public record NestedLine(String right) implements ErrorLine {
	@Override
	public String right() {
		return right;
	}
}
