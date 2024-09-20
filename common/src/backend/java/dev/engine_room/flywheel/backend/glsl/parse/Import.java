package dev.engine_room.flywheel.backend.glsl.parse;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.google.common.collect.ImmutableList;

import dev.engine_room.flywheel.backend.glsl.SourceLines;
import dev.engine_room.flywheel.backend.glsl.span.Span;

public record Import(Span self, Span file) {
	public static final Pattern PATTERN = Pattern.compile("^\\s*#\\s*include\\s+\"(.*)\"", Pattern.MULTILINE);

	/**
	 * Scan the source for {@code #include "..."} directives.
	 * Records the contents of the directive into an {@link Import} object, and marks the directive for elision.
	 */
	public static ImmutableList<Import> parseImports(SourceLines source) {
		Matcher matcher = PATTERN.matcher(source);

		var imports = ImmutableList.<Import>builder();

		while (matcher.find()) {
			Span use = Span.fromMatcher(source, matcher);
			Span file = Span.fromMatcher(source, matcher, 1);

			imports.add(new Import(use, file));
		}

		return imports.build();
	}
}
