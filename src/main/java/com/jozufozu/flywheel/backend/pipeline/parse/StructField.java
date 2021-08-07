package com.jozufozu.flywheel.backend.pipeline.parse;

import java.util.regex.Pattern;

import com.jozufozu.flywheel.backend.pipeline.error.ErrorReporter;
import com.jozufozu.flywheel.backend.pipeline.span.Span;

public class StructField extends AbstractShaderElement {
	public static final Pattern fieldPattern = Pattern.compile("(\\S+)\\s*(\\S+);");

	public Span type;
	public Span name;

	public StructField(Span self, Span type, Span name) {
		super(self);
		this.type = type;
		this.name = name;
	}

	public Span getType() {
		return type;
	}

	public Span getName() {
		return name;
	}

	@Override
	public String toString() {
		return type + " " + name;
	}
}
