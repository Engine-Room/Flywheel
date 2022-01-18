package com.jozufozu.flywheel.core.source.error.lines;

public record TextLine(String msg) implements ErrorLine {

	@Override
	public String build() {
		return msg;
	}
}
