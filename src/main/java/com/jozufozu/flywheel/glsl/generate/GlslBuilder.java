package com.jozufozu.flywheel.glsl.generate;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class GlslBuilder {
	private final List<Declaration> elements = new ArrayList<>();

	public void define(String name, String value) {
		add(new Define(name, value));
	}

	public void undef(String key) {
		add(new Undef(key));
	}

	public GlslStruct struct() {
		return add(new GlslStruct());
    }

    public GlslFn function() {
        return add(new GlslFn());
    }

    public GlslVertexInput vertexInput() {
        return add(new GlslVertexInput());
    }

    public GlslUniformBlock uniformBlock() {
        return add(new GlslUniformBlock());
    }

    public <T extends Declaration> T add(T element) {
        elements.add(element);
        return element;
    }

    public void blankLine() {
        elements.add(Separators.BLANK_LINE);
    }

	public void _addRaw(String sourceString) {
		elements.add(() -> sourceString);
	}

	public String build() {
		return elements.stream()
				.map(Declaration::prettyPrint)
				.collect(Collectors.joining("\n"));
	}

	public interface Declaration {
		String prettyPrint();
	}

	public enum Separators implements Declaration {
		BLANK_LINE(""),
		;

		private final String separator;

		Separators(String separator) {
			this.separator = separator;
		}

		@Override
		public String prettyPrint() {
			return separator;
		}
	}

	public record Define(String name, String value) implements Declaration {
		@Override
		public String prettyPrint() {
			return "#define " + name + " " + value;
		}
	}

	public record Undef(String name) implements Declaration {
		@Override
		public String prettyPrint() {
			return "#undef " + name;
		}
	}

}
