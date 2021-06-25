package com.jozufozu.flywheel.backend.pipeline.span;

import com.jozufozu.flywheel.backend.pipeline.SourceFile;

public class StringSpan extends Span {

	public StringSpan(SourceFile in, int start, int end) {
		super(in, start, end);
	}

	@Override
	public String getValue() {
		return in.getSource().substring(start, end);
	}
}
