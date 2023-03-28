package com.jozufozu.flywheel.core.source.parse;

import java.util.regex.Pattern;

import com.jozufozu.flywheel.core.source.span.Span;

public class Import {

	public static final Pattern PATTERN = Pattern.compile("^\\s*#\\s*use\\s+\"(.*)\"", Pattern.MULTILINE);

	public final Span self;
	public final Span file;

	public Import(Span self, Span file) {
		this.self = self;
		this.file = file;
	}

}
