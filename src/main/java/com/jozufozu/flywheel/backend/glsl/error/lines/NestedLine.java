package com.jozufozu.flywheel.backend.glsl.error.lines;

public record NestedLine(String right) implements ErrorLine {
	@Override
	public String right() {
		return right;
	}

	@Override
	public com.jozufozu.flywheel.glsl.error.lines.Divider divider() {
		return com.jozufozu.flywheel.glsl.error.lines.Divider.EQUALS;
	}
}
