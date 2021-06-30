package com.jozufozu.flywheel.backend.pipeline.span;

import com.jozufozu.flywheel.backend.pipeline.SourceFile;

public class StringSpan extends Span {

	public StringSpan(SourceFile in, int start, int end) {
		super(in, start, end);
	}

	@Override
	public Span subSpan(int from, int to) {
		return new StringSpan(in, start + from, start + to);
	}

	@Override
	public String get() {
		return in.getSource()
				.substring(start, end);
	}

	@Override
	public boolean isErr() {
		return false;
	}
}
