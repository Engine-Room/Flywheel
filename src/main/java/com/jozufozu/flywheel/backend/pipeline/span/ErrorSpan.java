package com.jozufozu.flywheel.backend.pipeline.span;

import com.jozufozu.flywheel.backend.pipeline.SourceFile;

public class ErrorSpan extends Span {
	public ErrorSpan(SourceFile in, int loc) {
		super(in, loc, loc);
	}

	public ErrorSpan(SourceFile in, int start, int end) {
		super(in, start, end);
	}

	@Override
	public String getValue() {
		return "";
	}
}
