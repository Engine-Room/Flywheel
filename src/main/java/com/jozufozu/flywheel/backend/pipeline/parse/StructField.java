package com.jozufozu.flywheel.backend.pipeline.parse;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.jozufozu.flywheel.backend.loading.LayoutTag;
import com.jozufozu.flywheel.backend.pipeline.span.Span;

public class StructField extends AbstractShaderElement {
	public static final Pattern fieldPattern = Pattern.compile("(\\S+)\\s*(\\S+);");

	public Span name;
	public Span type;

	public StructField(Span self, Span name, Span type) {
		super(self);
		this.name = name;
		this.type = type;
	}

	public Span getName() {
		return name;
	}

	public Span getType() {
		return type;
	}

	@Override
	public String toString() {
		return "TaggedField{" + "name='" + name + '\'' + ", type='" + type + '\'' + '}';
	}

	@Override
	public void checkErrors(ErrorReporter e) {

	}
}
