package com.jozufozu.flywheel.backend.pipeline;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.jozufozu.flywheel.backend.ShaderSources;
import com.jozufozu.flywheel.backend.pipeline.parse.Include;
import com.jozufozu.flywheel.backend.pipeline.parse.ShaderFunction;
import com.jozufozu.flywheel.backend.pipeline.span.CharPos;
import com.jozufozu.flywheel.backend.pipeline.span.ErrorSpan;
import com.jozufozu.flywheel.backend.pipeline.span.Span;
import com.jozufozu.flywheel.backend.pipeline.span.StringSpan;

import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import net.minecraft.util.ResourceLocation;

public class SourceFile {
	// #use "valid_namespace:valid/path_to_file.glsl"
	private static final Pattern includePattern = Pattern.compile("#use \"(\\w+:[\\w./]+)\"");

	private static final Pattern newLine = Pattern.compile("(\\r\\n|\\r|\\n)");

	// https://regexr.com/60n3d
	public static final Pattern functionDeclaration = Pattern.compile("(\\w+)\\s+(\\w+)\\s*\\(([\\w,\\s]*)\\)\\s*\\{");

	public final ResourceLocation name;

	private final ShaderSources parent;
	private final String source;

	private final IntList lineStarts;

	// Function name -> Function object
	private final ImmutableMap<String, ShaderFunction> functions;

	// Includes ordered as defined in the source
	private final ImmutableList<Include> includes;

	// Sections of the source that must be trimmed for compilation.
	private final List<Span> elisions = new ArrayList<>();

	public SourceFile(ShaderSources parent, ResourceLocation name, String source) {
		this.parent = parent;
		this.name = name;
		this.source = source;

		this.lineStarts = getLinePositions();

		this.functions = parseFunctions();
		this.includes = parseIncludes();
	}

	public String getSource() {
		return source;
	}

	public ShaderSources getParent() {
		return parent;
	}

	public ImmutableMap<String, ShaderFunction> getFunctions() {
		return functions;
	}

	public ImmutableList<Include> getIncludes() {
		return includes;
	}

	public CharPos getCharPos(int charPos) {
		int lineNo = 0;
		for (; lineNo < lineStarts.size(); lineNo++) {
			int ls = lineStarts.getInt(lineNo);

			if (charPos < ls) {
				break;
			}
		}

		int lineStart = lineStarts.getInt(lineNo - 1);

		return new CharPos(charPos, lineNo, charPos - lineStart);
	}

	public String printSource() {
		StringBuilder builder = new StringBuilder();

		builder.append("Source for shader '")
				.append(name)
				.append("':\n");
		int i = 1;
		for (String s : source.split("\n")) {
			builder.append(String.format("%1$4s: ", i++))
					.append(s)
					.append('\n');
		}

		return builder.toString();
	}

	private CharSequence elided = null;

	public CharSequence getElidedSource() {
		if (elided == null) {
			StringBuilder out = new StringBuilder();

			int lastEnd = 0;

			for (Span elision : elisions) {
				out.append(source, lastEnd, elision.getStart());

				lastEnd = elision.getEnd();
			}

			out.append(source, lastEnd, source.length());

			elided = out.toString();
		}

		return elided;
	}

	/**
	 * Scan the source for line breaks, recording the position of the first character of each line.
	 */
	private IntList getLinePositions() {
		IntList l = new IntArrayList();
		l.add(0); // first line is always at position 0

		Matcher matcher = newLine.matcher(source);

		while (matcher.find()) {
			l.add(matcher.end());
		}
		return l;
	}

	/**
	 * Scan the source for function definitions and "parse" them into objects that contain properties of the function.
	 */
	private ImmutableMap<String, ShaderFunction> parseFunctions() {
		Matcher matcher = functionDeclaration.matcher(source);

		Map<String, ShaderFunction> functions = new HashMap<>();

		while (matcher.find()) {
			Span type = Span.fromMatcher(this, matcher, 1);
			Span name = Span.fromMatcher(this, matcher, 2);
			Span args = Span.fromMatcher(this, matcher, 3);

			int blockStart = matcher.end();
			int blockEnd = findEndOfBlock(blockStart);

			Span self;
			Span body;
			if (blockEnd > blockStart) {
				self = new StringSpan(this, matcher.start(), blockEnd + 1);
				body = new StringSpan(this, blockStart, blockEnd);
			} else {
				self = new ErrorSpan(this, matcher.start(), matcher.end());
				body = new ErrorSpan(this, blockStart);
			}

			ShaderFunction function = new ShaderFunction(self, type, name, args, body);

			functions.put(name.get(), function);
		}

		return ImmutableMap.copyOf(functions);
	}

	/**
	 * Scan the source for <code>#use "..."</code> directives.
	 * Records the contents of the directive into an {@link Include} object, and marks the directive for elision.
	 */
	private ImmutableList<Include> parseIncludes() {
		Matcher uses = includePattern.matcher(source);

		List<Include> includes = new ArrayList<>();

		while (uses.find()) {
			Span use = Span.fromMatcher(this, uses);
			Span file = Span.fromMatcher(this, uses, 1);

			includes.add(new Include(parent, use, file));

			elisions.add(use); // we have to trim that later
		}

		return ImmutableList.copyOf(includes);
	}

	/**
	 * Given the position of an opening brace, scans through the source for a paired closing brace.
	 */
	private int findEndOfBlock(int start) {
		int blockDepth = 0;
		for (int i = start + 1; i < source.length(); i++) {
			char ch = source.charAt(i);

			if (ch == '{') blockDepth++;
			else if (ch == '}') blockDepth--;

			if (blockDepth < 0) {
				return i;
			}
		}

		return -1;
	}
}
