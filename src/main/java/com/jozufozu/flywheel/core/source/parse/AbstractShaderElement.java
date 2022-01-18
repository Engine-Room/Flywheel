package com.jozufozu.flywheel.core.source.parse;

import com.jozufozu.flywheel.core.source.span.Span;

public abstract class AbstractShaderElement {

	public final Span self;

	public AbstractShaderElement(Span self) {
		this.self = self;
	}

}
