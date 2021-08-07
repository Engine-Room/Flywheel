package com.jozufozu.flywheel.backend.pipeline.parse;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import com.google.common.collect.ImmutableList;
import com.jozufozu.flywheel.backend.pipeline.span.Span;

public class ShaderFunction extends AbstractShaderElement {

	public static final Pattern argument = Pattern.compile("(\\w+)\\s+(\\w+)");
	public static final Pattern assignment = Pattern.compile("(\\w+)\\s*=");

	private final Span type;
	private final Span name;
	private final Span args;
	private final Span body;

	private final ImmutableList<Variable> parameters;

	public ShaderFunction(Span self, Span type, Span name, Span args, Span body) {
		super(self);
		this.type = type;
		this.name = name;
		this.args = args;
		this.body = body;

		this.parameters = parseArguments();
	}

	public String call(String... args) {
		return name + "(" + String.join(", ", args) + ")";
	}

	public ImmutableList<Variable> getParameters() {
		return parameters;
	}

	public String returnType() {
		return type.get();
	}

	protected ImmutableList<Variable> parseArguments() {
		if (args.isErr() || args.isEmpty()) return ImmutableList.of();

		Matcher arguments = argument.matcher(args.get());

		ImmutableList.Builder<Variable> builder = ImmutableList.builder();

		while (arguments.find()) {
			Span self = Span.fromMatcher(args, arguments);
			Span type = Span.fromMatcher(args, arguments, 1);
			Span name = Span.fromMatcher(args, arguments, 2);

			builder.add(new Variable(self, type, name));
		}

		return builder.build();
	}

	@Override
	public String toString() {

		String p = parameters.stream()
				.map(Variable::typeName)
				.map(Span::get)
				.collect(Collectors.joining(","));

		return type + " " + name + "(" + p + ")";
	}
}
