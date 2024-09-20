package dev.engine_room.flywheel.backend.glsl.parse;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.jetbrains.annotations.Nullable;

import com.google.common.collect.ImmutableMap;

import dev.engine_room.flywheel.backend.glsl.SourceLines;
import dev.engine_room.flywheel.backend.glsl.span.Span;

public class ShaderField {
	public static final Pattern PATTERN = Pattern.compile("layout\\s*\\(location\\s*=\\s*(\\d+)\\)\\s+(in|out)\\s+([\\w\\d]+)\\s+" + "([\\w\\d]+)");

	public final Span self;
	public final Span location;
	public final Span qualifierSpan;
	@Nullable
	public final Qualifier qualifier;
	public final Span type;
	public final Span name;

	public ShaderField(Span self, Span location, Span qualifier, Span type, Span name) {
		this.self = self;
		this.location = location;
		this.qualifierSpan = qualifier;
		this.qualifier = Qualifier.fromSpan(qualifier);
		this.type = type;
		this.name = name;
	}

	/**
	 * Scan the source for function definitions and "parse" them into objects that contain properties of the function.
	 */
	public static ImmutableMap<String, ShaderField> parseFields(SourceLines source) {
		Matcher matcher = PATTERN.matcher(source);

		ImmutableMap.Builder<String, ShaderField> fields = ImmutableMap.builder();
		while (matcher.find()) {
			Span self = Span.fromMatcher(source, matcher);
			Span location = Span.fromMatcher(source, matcher, 1);
			Span decoration = Span.fromMatcher(source, matcher, 2);
			Span type = Span.fromMatcher(source, matcher, 3);
			Span name = Span.fromMatcher(source, matcher, 4);

			fields.put(location.get(), new ShaderField(self, location, decoration, type, name));
		}

		return fields.build();
	}

	public enum Qualifier {
		IN,
		OUT,
		FLAT,
		;

		@Nullable
		public static Qualifier fromSpan(Span span) {
			return switch (span.toString()) {
				case "in" -> IN;
				case "out" -> OUT;
				case "flat" -> FLAT;
				default -> null;
			};
		}
	}
}
