package com.jozufozu.flywheel.backend.glsl.error.lines;

public record FileLine(String fileName) implements ErrorLine {

	@Override
	public String left() {
		return "-";
	}

	@Override
	public com.jozufozu.flywheel.glsl.error.lines.Divider divider() {
		return com.jozufozu.flywheel.glsl.error.lines.Divider.ARROW;
	}

	@Override
	public String right() {
		return fileName;
	}
}
