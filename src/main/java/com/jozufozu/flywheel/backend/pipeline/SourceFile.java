package com.jozufozu.flywheel.backend.pipeline;

import java.io.BufferedReader;
import java.io.StringReader;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import com.jozufozu.flywheel.backend.ShaderSources;

import com.jozufozu.flywheel.backend.pipeline.span.ErrorSpan;
import com.jozufozu.flywheel.backend.pipeline.span.Span;

import com.jozufozu.flywheel.backend.pipeline.span.StringSpan;

import net.minecraft.util.ResourceLocation;

public class SourceFile {
	// #use "valid_namespace:valid/path_to_file.glsl"
	private static final Pattern includePattern = Pattern.compile("#use \"(\\w+:[\\w./]+)\"");

	// https://regexr.com/60n3d
	public static final Pattern functionDeclaration = Pattern.compile("(\\w+)\\s+(\\w+)\\s*\\(([\\w,\\s]*)\\)\\s*\\{");

	public static final Pattern versionDetector = Pattern.compile("#version[^\\n]*");

	public final ResourceLocation name;
	private final String source;
	private final ShaderSources loader;

	private final Map<String, ShaderFunction> functions = new HashMap<>();

	public SourceFile(ShaderSources loader, ResourceLocation name, String source) {
		this.loader = loader;
		this.name = name;
		this.source = source;

		parseFunctions();
	}

	public String getSource() {
		return source;
	}

	protected void parseFunctions() {
		Matcher matcher = functionDeclaration.matcher(source);

		while (matcher.find()) {
			Span type = Span.fromMatcherGroup(this, matcher, 1);
			Span name = Span.fromMatcherGroup(this, matcher, 2);

			int blockStart = matcher.end();
			int blockEnd = findEndOfBlock(blockStart);

			Span self;
			Span body;
			if (blockEnd > blockStart) {
				self = new StringSpan(this, matcher.start(), blockEnd);
				body = new StringSpan(this, blockStart, blockEnd);
			} else {
				self = new ErrorSpan(this, matcher.start(), matcher.end());
				body = new ErrorSpan(this, blockStart);
			}

			ShaderFunction function = new ShaderFunction(self, type, name, body);

			functions.put(name.getValue(), function);
		}
	}

	private int findEndOfBlock(int end) {
		char[] rest = source.substring(end).toCharArray();

		int blockDepth = 0;
		for (int i = 0; i < rest.length; i++) {
			char ch = rest[i];

			if (ch == '{') blockDepth++;
			if (ch == '}') blockDepth--;

			if (blockDepth < 0) {
				return end + i;
			}
		}

		return -1;
	}

	public String printSource() {
		StringBuilder builder = new StringBuilder();

		builder.append("Source for shader '").append(name).append("':\n");
		int i = 1;
		for (String s : source.split("\n")) {
			builder.append(String.format("%1$4s: ", i++))
				   .append(s)
				   .append('\n');
		}

		return builder.toString();
	}

	public static Stream<String> lines(String s) {
		return new BufferedReader(new StringReader(s)).lines();
	}
}
