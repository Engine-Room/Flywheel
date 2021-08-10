package com.jozufozu.flywheel.backend.source.parse;

import com.jozufozu.flywheel.backend.source.span.Span;

public class Variable extends AbstractShaderElement {

	private final Span type;
	private final Span name;

	public Variable(Span self, Span type, Span name) {
		super(self);
		this.type = type;
		this.name = name;
	}

	public Span typeName() {
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
