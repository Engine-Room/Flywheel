package dev.engine_room.flywheel.backend.glsl.span;

import dev.engine_room.flywheel.backend.glsl.SourceLines;

public class StringSpan extends Span {

	public StringSpan(SourceLines in, int start, int end) {
		super(in, start, end);
	}

	@Override
	public Span subSpan(int from, int to) {
		return new StringSpan(in, start.pos() + from, start.pos() + to);
	}

	@Override
	public String get() {
		return in.raw.substring(start.pos(), end.pos());
	}

	@Override
	public boolean isErr() {
		return false;
	}
}
