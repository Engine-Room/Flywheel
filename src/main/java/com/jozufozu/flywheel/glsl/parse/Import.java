package com.jozufozu.flywheel.glsl.parse;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.google.common.collect.ImmutableList;
import com.jozufozu.flywheel.glsl.SourceLines;
import com.jozufozu.flywheel.glsl.span.Span;

public record Import(Span self, Span file) {
	public static final Pattern PATTERN = Pattern.compile("^\\s*#\\s*include\\s+\"(.*)\"", Pattern.MULTILINE);

	/**
	 * Scan the source for {@code #use "..."} directives.
	 * Records the contents of the directive into an {@link Import} object, and marks the directive for elision.
	 */
	public static ImmutableList<Import> parseImports(SourceLines source) {
		Matcher uses = PATTERN.matcher(source);

		var imports = ImmutableList.<Import>builder();

		while (uses.find()) {
			Span use = Span.fromMatcher(source, uses);
			Span file = Span.fromMatcher(source, uses, 1);

			imports.add(new Import(use, file));
		}

		return imports.build();
	}
}
