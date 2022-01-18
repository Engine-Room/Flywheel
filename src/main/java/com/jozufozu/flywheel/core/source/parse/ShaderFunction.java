package com.jozufozu.flywheel.core.source.parse;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import com.google.common.collect.ImmutableList;
import com.jozufozu.flywheel.core.source.span.Span;

public class ShaderFunction extends AbstractShaderElement {

	public static final Pattern argument = Pattern.compile("(?:(inout|in|out) )?(\\w+)\\s+(\\w+)");
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

	public Span getType() {
		return type;
	}

	public Span getName() {
		return name;
	}

	public Span getArgs() {
		return args;
	}

	public Span getBody() {
		return body;
	}

	public String call(String... args) {
		return name + "(" + String.join(", ", args) + ")";
	}

	public ImmutableList<Variable> getParameters() {
		return parameters;
	}

	public String returnTypeName() {
		return type.get();
	}

	protected ImmutableList<Variable> parseArguments() {
		if (args.isErr() || args.isEmpty()) return ImmutableList.of();

		Matcher arguments = argument.matcher(args.get());

		ImmutableList.Builder<Variable> builder = ImmutableList.builder();

		while (arguments.find()) {
			Span self = Span.fromMatcher(args, arguments);
			Span qualifier = Span.fromMatcher(args, arguments, 1);
			Span type = Span.fromMatcher(args, arguments, 2);
			Span name = Span.fromMatcher(args, arguments, 3);

			builder.add(new Variable(self, qualifier, type, name));
		}

		return builder.build();
	}

	@Override
	public String toString() {

		String p = parameters.stream()
				.map(variable -> variable.type)
				.map(Span::get)
				.collect(Collectors.joining(","));

		return type + " " + name + "(" + p + ")";
	}
}
