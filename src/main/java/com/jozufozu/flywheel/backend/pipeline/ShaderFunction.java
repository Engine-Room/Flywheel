package com.jozufozu.flywheel.backend.pipeline;

import com.jozufozu.flywheel.backend.pipeline.span.Span;

import java.util.regex.Pattern;

public class ShaderFunction {

	public static final Pattern assignment = Pattern.compile("(\\w+)\\s*=");

	private final Span returnType;
	private final Span name;
	private final Span body;

	public ShaderFunction(Span self, Span returnType, Span name, Span body) {
		this.returnType = returnType;
		this.name = name;
		this.body = body;
	}
}
