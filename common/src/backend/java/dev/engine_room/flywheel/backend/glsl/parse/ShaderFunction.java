package dev.engine_room.flywheel.backend.glsl.parse;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;

import dev.engine_room.flywheel.backend.glsl.SourceLines;
import dev.engine_room.flywheel.backend.glsl.span.ErrorSpan;
import dev.engine_room.flywheel.backend.glsl.span.Span;
import dev.engine_room.flywheel.backend.glsl.span.StringSpan;

public class ShaderFunction {
	// https://regexr.com/60n3d
	public static final Pattern PATTERN = Pattern.compile("(\\w+)\\s+(\\w+)\\s*\\(([\\w,\\s]*)\\)\\s*\\{");
	public static final Pattern ARGUMENT_PATTERN = Pattern.compile("(?:(inout|in|out) )?(\\w+)\\s+(\\w+)");
	public static final Pattern ASSIGNMENT_PATTERN = Pattern.compile("(\\w+)\\s*=");

	public final Span self;
	public final Span type;
	public final Span name;
	public final Span args;
	public final Span body;

	public final ImmutableList<ShaderVariable> parameters;

	public ShaderFunction(Span self, Span type, Span name, Span args, Span body) {
		this.self = self;
		this.type = type;
		this.name = name;
		this.args = args;
		this.body = body;

		this.parameters = parseArguments();
	}

	/**
	 * Scan the source for function definitions and "parse" them into objects that contain properties of the function.
	 */
	public static ImmutableMap<String, ShaderFunction> parseFunctions(SourceLines source) {
		Matcher matcher = PATTERN.matcher(source);

		Map<String, ShaderFunction> functions = new HashMap<>();

		while (matcher.find()) {
			Span type = Span.fromMatcher(source, matcher, 1);
			Span name = Span.fromMatcher(source, matcher, 2);
			Span args = Span.fromMatcher(source, matcher, 3);

			int blockStart = matcher.end();
			int blockEnd = findEndOfBlock(source, blockStart);

			Span self;
			Span body;
			if (blockEnd > blockStart) {
				self = new StringSpan(source, matcher.start(), blockEnd + 1);
				body = new StringSpan(source, blockStart, blockEnd);
			} else {
				self = new ErrorSpan(source, matcher.start(), matcher.end());
				body = new ErrorSpan(source, blockStart);
			}

			ShaderFunction function = new ShaderFunction(self, type, name, args, body);

			functions.put(name.get(), function);
		}

		return ImmutableMap.copyOf(functions);
	}

	/**
	 * Given the position of an opening brace, scans through the source for a paired closing brace.
	 */
	private static int findEndOfBlock(CharSequence source, int start) {
		int blockDepth = 0;
		for (int i = start + 1; i < source.length(); i++) {
			char ch = source.charAt(i);

			if (ch == '{') {
				blockDepth++;
			} else if (ch == '}') {
				blockDepth--;
			}

			if (blockDepth < 0) {
				return i;
			}
		}

		return -1;
	}

	public String call(String... args) {
		return name + "(" + String.join(", ", args) + ")";
	}

	public Span getParameterType(int index) {
		return parameters.get(index).type;
	}

	protected ImmutableList<ShaderVariable> parseArguments() {
		if (args.isErr() || args.isEmpty()) return ImmutableList.of();

		Matcher arguments = ARGUMENT_PATTERN.matcher(args.get());

		ImmutableList.Builder<ShaderVariable> builder = ImmutableList.builder();

		while (arguments.find()) {
			Span self = Span.fromMatcher(args, arguments);
			Span qualifier = Span.fromMatcher(args, arguments, 1);
			Span type = Span.fromMatcher(args, arguments, 2);
			Span name = Span.fromMatcher(args, arguments, 3);

			builder.add(new ShaderVariable(self, qualifier, type, name));
		}

		return builder.build();
	}

	@Override
	public String toString() {
		String p = parameters.stream()
				.map(variable -> variable.type)
				.map(Span::get)
				.collect(Collectors.joining(", "));

		return type + " " + name + "(" + p + ")";
	}
}
