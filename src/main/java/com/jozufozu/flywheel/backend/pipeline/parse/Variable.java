package com.jozufozu.flywheel.backend.pipeline.parse;

import com.jozufozu.flywheel.backend.pipeline.error.ErrorReporter;
import com.jozufozu.flywheel.backend.pipeline.span.Span;

public class Variable extends AbstractShaderElement {

	private final Span type;
	private final Span name;

	public Variable(Span self, Span type, Span name) {
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
	public void checkErrors(ErrorReporter e) {

	}
}
