package com.jozufozu.flywheel.backend.source.parse;

import com.jozufozu.flywheel.backend.source.span.Span;

public abstract class AbstractShaderElement {

	public final Span self;

	public AbstractShaderElement(Span self) {
		this.self = self;
	}

}
