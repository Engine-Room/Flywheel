package dev.engine_room.flywheel.backend.glsl.parse;

import java.util.regex.Pattern;

import dev.engine_room.flywheel.backend.glsl.span.Span;

public class StructField {
	public static final Pattern PATTERN = Pattern.compile("(\\S+)\\s*(\\S+);");

	public final Span self;
	public final Span type;
	public final Span name;

	public StructField(Span self, Span type, Span name) {
		this.self = self;
		this.type = type;
		this.name = name;
	}

	@Override
	public String toString() {
		return type + " " + name;
	}
}
