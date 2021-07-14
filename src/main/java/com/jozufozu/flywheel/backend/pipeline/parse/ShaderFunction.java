package com.jozufozu.flywheel.backend.pipeline.parse;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import com.jozufozu.flywheel.backend.pipeline.error.ErrorReporter;
import com.jozufozu.flywheel.backend.pipeline.span.Span;

public class ShaderFunction extends AbstractShaderElement {

	public static final Pattern argument = Pattern.compile("(\\w+)\\s+(\\w+)");
	public static final Pattern assignment = Pattern.compile("(\\w+)\\s*=");

	private final Span type;
	private final Span name;
	private final Span args;
	private final Span body;

	private final List<Variable> parameters;

	public ShaderFunction(Span self, Span type, Span name, Span args, Span body) {
		super(self);
		this.type = type;
		this.name = name;
		this.args = args;
		this.body = body;

		this.parameters = new ArrayList<>();

		parseArguments();
	}

	public String call(String... args) {
		return name + "(" + String.join(", ", args) + ");";
	}

	protected void parseArguments() {
		if (args.isErr() || args.isEmpty()) return;

		Matcher arguments = argument.matcher(args.get());

		while (arguments.find()) {
			Span self = Span.fromMatcher(args, arguments);
			Span type = Span.fromMatcher(args, arguments, 1);
			Span name = Span.fromMatcher(args, arguments, 2);

			parameters.add(new Variable(self, type, name));
		}
	}

	@Override
	public String toString() {

		String p = parameters.stream()
				.map(Variable::getType)
				.map(Span::get)
				.collect(Collectors.joining(","));

		return type + " " + name + "(" + p + ")";
	}
}
