package com.jozufozu.flywheel.core.source.parse;

import com.jozufozu.flywheel.core.source.span.Span;

public class Variable extends AbstractShaderElement {

	public final Span qualifierSpan;
	public final Span type;
	public final Span name;
	public final Qualifier qualifier;

	public Variable(Span self, Span qualifier, Span type, Span name) {
		super(self);
		this.qualifierSpan = qualifier;
		this.type = type;
		this.name = name;
		this.qualifier = Qualifier.fromSpan(qualifierSpan);
	}

	@Override
	public String toString() {
		return type + " " + name;
	}

	public enum Qualifier {
		NONE,
		IN,
		OUT,
		INOUT,
		ERROR;

		public static Qualifier fromSpan(Span s) {
			String span = s.toString();

			return switch (span) {
				case "" -> NONE;
				case "in" -> IN;
				case "inout" -> INOUT;
				case "out" -> OUT;
				default -> ERROR;
			};
		}
	}
}
