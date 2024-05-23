package dev.engine_room.flywheel.backend.glsl.parse;

import java.util.regex.Pattern;

import dev.engine_room.flywheel.backend.glsl.span.Span;

public class StructField {
	public static final Pattern fieldPattern = Pattern.compile("(\\S+)\\s*(\\S+);");
	public final Span self;

	public Span type;
	public Span name;

	public StructField(Span self, Span type, Span name) {
		this.self = self;
		this.type = type;
		this.name = name;
	}

	public Span getType() {
		return type;
	}

	public Span getName() {
		return name;
	}

	@Override
	public String toString() {
		return type + " " + name;
	}
}
