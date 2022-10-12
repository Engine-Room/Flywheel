package com.jozufozu.flywheel.core.source.generate;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import com.jozufozu.flywheel.util.Pair;

public class GlslBuilder {
	private final List<GlslRootElement> elements = new ArrayList<>();

	public void define(String name, String value) {
		add(new Define(name, value));
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

	public <T extends GlslRootElement> T add(T element) {
		elements.add(element);
		return element;
	}

	public void blankLine() {
		elements.add(Separators.BLANK_LINE);
	}

	public String build() {
		return elements.stream()
				.map(GlslRootElement::prettyPrint)
				.collect(Collectors.joining("\n"));
	}

	public interface GlslRootElement {
		String prettyPrint();
	}

	public enum Separators implements GlslRootElement {
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

	public record Define(String name, String value) implements GlslRootElement {
		@Override
		public String prettyPrint() {
			return "#define " + name + " " + value;
		}
	}

	public static class VertexInputBuilder implements GlslRootElement {

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
		public String prettyPrint() {
			return "layout(location = " + binding + ") in " + type + " " + name + ";";
		}
	}

	public static class StructBuilder implements GlslRootElement {

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
					.map(p -> p.first() + ' ' + p.second() + ';')
					.collect(Collectors.joining("\n"));
		}

		public String prettyPrint() {
			return """
					struct %s {
					%s
					};
					""".formatted(name, buildFields().indent(4));
		}
	}

	public static class FunctionBuilder implements GlslRootElement {
		private final List<Pair<String, String>> arguments = new ArrayList<>();
		private final BlockBuilder body = new BlockBuilder();
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

		public FunctionBuilder body(Consumer<BlockBuilder> f) {
			f.accept(body);
			return this;
		}

		public String prettyPrint() {
			return """
					%s %s(%s) {
					%s
					}
					""".formatted(returnType, name, buildArguments(), body.prettyPrint()
					.indent(4));
		}

		private String buildArguments() {
			return arguments.stream()
					.map(p -> p.first() + ' ' + p.second())
					.collect(Collectors.joining(", "));
		}
	}

	public static class BlockBuilder implements LangItem {
		private final List<GlslStmt> body = new ArrayList<>();

		public BlockBuilder add(GlslStmt stmt) {
			body.add(stmt);
			return this;
		}

		public BlockBuilder eval(GlslExpr expr) {
			return add(GlslStmt.eval(expr));
		}

		public BlockBuilder switchOn(GlslExpr expr, Consumer<SwitchBuilder> f) {
			var builder = new SwitchBuilder(expr);
			f.accept(builder);
			return add(builder.build());
		}

		public void ret(GlslExpr call) {
			add(GlslStmt.ret(call));
		}

		public void break_() {
			add(GlslStmt.BREAK);
		}

		@Override
		public String prettyPrint() {
			return body.stream()
					.map(GlslStmt::prettyPrint)
					.collect(Collectors.joining("\n"));
		}

	}

	public static class SwitchBuilder {

		private final GlslExpr on;

		private final List<Pair<GlslExpr, BlockBuilder>> cases = new ArrayList<>();

		public SwitchBuilder(GlslExpr on) {
			this.on = on;
		}

		public SwitchBuilder case_(int expr, Consumer<BlockBuilder> f) {
			var builder = new BlockBuilder();
			f.accept(builder);
			cases.add(Pair.of(GlslExpr.literal(expr), builder));
			return this;
		}

		public GlslStmt.Switch build() {
			return new GlslStmt.Switch(on, cases);
		}
	}
}
