package com.jozufozu.flywheel.core.source.generate;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import com.jozufozu.flywheel.util.Pair;

public class GlslBuilder {

	private final List<SourceElement> elements = new ArrayList<>();

	public void define(String name, String value) {
		elements.add(new Define(name, value));
	}

	public StructBuilder struct() {
		return add(new StructBuilder());
	}

	public FunctionBuilder function() {
		return add(new FunctionBuilder());
	}

	public VertexInputBuilder vertexInput() {
		return add(new VertexInputBuilder());
	}

	public <T extends SourceElement> T add(T element) {
		elements.add(element);
		return element;
	}


	public String build() {
		return elements.stream()
				.map(SourceElement::build)
				.collect(Collectors.joining("\n"));
	}

	public void blankLine() {
		elements.add(Separators.BLANK_LINE);
	}

	public enum Separators implements SourceElement {
		BLANK_LINE(""),
		;

		private final String separator;

		Separators(String separator) {
			this.separator = separator;
		}

		@Override
		public String build() {
			return separator;
		}
	}

	public interface SourceElement {
		String build();
	}

	public record Define(String name, String value) implements SourceElement {
		@Override
		public String build() {
			return "#define " + name + " " + value;
		}
	}

	public static class VertexInputBuilder implements SourceElement {

		private int binding;
		private String type;
		private String name;

		public VertexInputBuilder binding(int binding) {
			this.binding = binding;
			return this;
		}

		public VertexInputBuilder type(String type) {
			this.type = type;
			return this;
		}

		public VertexInputBuilder name(String name) {
			this.name = name;
			return this;
		}

		@Override
		public String build() {
			return "layout(location = " + binding + ") in " + type + " " + name + ";";
		}
	}

	public static class StructBuilder implements SourceElement {

		private final List<Pair<String, String>> fields = new ArrayList<>();
		private String name;

		public void setName(String name) {
			this.name = name;
		}

		public void addField(String type, String name) {
			fields.add(Pair.of(type, name));
		}

		private String buildFields() {
			return fields.stream()
					.map(p -> '\t' + p.first() + ' ' + p.second() + ';')
					.collect(Collectors.joining("\n"));
		}

		public String build() {
			return """
					struct %s {
					%s
					};
					""".formatted(name, buildFields());
		}
	}

	public static class FunctionBuilder implements SourceElement {
		private final List<Pair<String, String>> arguments = new ArrayList<>();
		private final List<String> body = new ArrayList<>();
		private String returnType;
		private String name;

		public FunctionBuilder returnType(String returnType) {
			this.returnType = returnType;
			return this;
		}

		public FunctionBuilder name(String name) {
			this.name = name;
			return this;
		}

		public FunctionBuilder argument(String type, String name) {
			arguments.add(Pair.of(type, name));
			return this;
		}

		public FunctionBuilder argumentIn(String type, String name) {
			arguments.add(Pair.of("in " + type, name));
			return this;
		}

		public FunctionBuilder statement(String statement) {
			this.body.add(statement);
			return this;
		}


		public String build() {
			return """
					%s %s(%s) {
					%s
					}
					""".formatted(returnType, name, buildArguments(), buildBody());
		}

		private String buildBody() {
			return body.stream()
					.map(s -> '\t' + s)
					.collect(Collectors.joining("\n"));
		}

		private String buildArguments() {
			return arguments.stream()
					.map(p -> p.first() + ' ' + p.second())
					.collect(Collectors.joining(", "));
		}
	}
}
