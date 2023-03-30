package com.jozufozu.flywheel.glsl.error.lines;

public record TextLine(String msg) implements ErrorLine {

	@Override
	public String build() {
		return msg;
	}
}
