package com.jozufozu.flywheel.glsl.parse;

import java.util.regex.Pattern;

import com.jozufozu.flywheel.glsl.span.Span;

public record Import(Span self, Span file) {
	public static final Pattern PATTERN = Pattern.compile("^\\s*#\\s*use\\s+\"(.*)\"", Pattern.MULTILINE);
}
