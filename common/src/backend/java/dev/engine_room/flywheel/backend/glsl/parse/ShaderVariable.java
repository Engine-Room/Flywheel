package dev.engine_room.flywheel.backend.glsl.parse;

import org.jetbrains.annotations.Nullable;

import dev.engine_room.flywheel.backend.glsl.span.Span;

public class ShaderVariable {
	public final Span self;
	public final Span qualifierSpan;
	@Nullable
	public final Qualifier qualifier;
	public final Span type;
	public final Span name;

	public ShaderVariable(Span self, Span qualifier, Span type, Span name) {
		this.self = self;
		this.qualifierSpan = qualifier;
		this.qualifier = Qualifier.fromSpan(qualifierSpan);
		this.type = type;
		this.name = name;
	}

	@Override
	public String toString() {
		return type + " " + name;
	}

	public enum Qualifier {
		NONE,
		IN,
		OUT,
		INOUT;

		@Nullable
		public static Qualifier fromSpan(Span s) {
			String span = s.toString();

			return switch (span) {
				case "" -> NONE;
				case "in" -> IN;
				case "inout" -> INOUT;
				case "out" -> OUT;
				default -> null;
			};
		}
	}
}
