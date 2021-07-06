package com.jozufozu.flywheel.backend.pipeline.span;

import com.jozufozu.flywheel.backend.pipeline.SourceFile;

public class ErrorSpan extends Span {
	public ErrorSpan(SourceFile in, int loc) {
		super(in, loc, loc);
	}

	public ErrorSpan(SourceFile in, int start, int end) {
		super(in, start, end);
	}

	public ErrorSpan(SourceFile in, CharPos start, CharPos end) {
		super(in, start, end);
	}

	@Override
	public Span subSpan(int from, int to) {
		return new ErrorSpan(in, start, end); // the sub-span of an error is an error in the same location
	}

	@Override
	public String get() {
		return "";
	}

	@Override
	public boolean isErr() {
		return true;
	}
}
