package com.jozufozu.flywheel.glsl.parse;

import java.util.regex.Pattern;

import org.jetbrains.annotations.Nullable;

import com.jozufozu.flywheel.glsl.span.Span;

public class ShaderField {
	public static final Pattern PATTERN = Pattern.compile("layout\\s*\\(location\\s*=\\s*(\\d+)\\)\\s+(in|out)\\s+([\\w\\d]+)\\s+" + "([\\w\\d]+)");

	public final Span location;
	public final @Nullable Decoration decoration;
	public final Span type;
	public final Span name;
	public final Span self;

	public ShaderField(Span self, Span location, Span inOut, Span type, Span name) {
		this.self = self;

		this.location = location;
		this.decoration = Decoration.fromSpan(inOut);
		this.type = type;
		this.name = name;
	}

	public enum Decoration {
		IN,
		OUT,
		FLAT,
		;

		@Nullable
		public static Decoration fromSpan(Span span) {
			return switch (span.toString()) {
				case "in" -> IN;
				case "out" -> OUT;
				case "flat" -> FLAT;
				default -> null;
			};
		}
	}
}
