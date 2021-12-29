package com.jozufozu.flywheel.backend.source.error.lines;

public record TextLine(String msg) implements ErrorLine {

	@Override
	public String build() {
		return msg;
	}
}
