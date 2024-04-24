package com.jozufozu.flywheel.backend.glsl.error.lines;

public record TextLine(String msg) implements ErrorLine {

	@Override
	public String build() {
		return msg;
	}
}
