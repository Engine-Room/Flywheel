package com.jozufozu.flywheel.backend.pipeline.parse;

import com.jozufozu.flywheel.backend.pipeline.span.Span;

public class Variable {

	private final Span self;
	private final Span type;
	private final Span name;

	public Variable(Span self, Span type, Span name) {
		this.self = self;
		this.type = type;
		this.name = name;
	}

	public Span getType() {
		return type;
	}

	public Span getName() {
		return name;
	}
}
