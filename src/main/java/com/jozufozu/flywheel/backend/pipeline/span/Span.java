package com.jozufozu.flywheel.backend.pipeline.span;

import com.jozufozu.flywheel.backend.pipeline.SourceFile;

import java.util.regex.Matcher;

public abstract class Span {

	protected final SourceFile in;
	protected final int start;
	protected final int end;

	public Span(SourceFile in, int start, int end) {
		this.in = in;
		this.start = start;
		this.end = end;
	}

	public SourceFile getSourceFile() {
		return in;
	}

	public int getStart() {
		return start;
	}

	public int getEnd() {
		return end;
	}

	public abstract String getValue();

	public static Span fromMatcherGroup(SourceFile src, Matcher m, int group) {
		return new StringSpan(src, m.start(group), m.end(group));
	}

	public static Span fromMatcher(SourceFile src, Matcher m) {
		return new StringSpan(src, m.start(), m.end());
	}
}
