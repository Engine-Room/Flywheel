package com.jozufozu.flywheel.backend.pipeline.parse;

import com.jozufozu.flywheel.backend.pipeline.span.Span;

public abstract class AbstractShaderElement {

	public final Span self;

	public AbstractShaderElement(Span self) {
		this.self = self;
	}

}
