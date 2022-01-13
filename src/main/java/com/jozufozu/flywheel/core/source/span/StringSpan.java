package com.jozufozu.flywheel.core.source.span;

import com.jozufozu.flywheel.core.source.SourceFile;

public class StringSpan extends Span {

	public StringSpan(SourceFile in, int start, int end) {
		super(in, start, end);
	}

	public StringSpan(SourceFile in, CharPos start, CharPos end) {
		super(in, start, end);
	}

	@Override
	public Span subSpan(int from, int to) {
        return new StringSpan(in, start.pos() + from, start.pos() + to);
	}

	@Override
	public String get() {
		return in.source
				.substring(start.pos(), end.pos());
	}

	@Override
	public boolean isErr() {
		return false;
	}
}
