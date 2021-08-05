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
import com.jozufozu.flywheel.backend.pipeline.error.ErrorReporter;
import com.jozufozu.flywheel.backend.pipeline.parse.AbstractShaderElement;
import com.jozufozu.flywheel.backend.pipeline.parse.Include;
import com.jozufozu.flywheel.backend.pipeline.parse.ShaderFunction;
import com.jozufozu.flywheel.backend.pipeline.parse.ShaderStruct;
import com.jozufozu.flywheel.backend.pipeline.span.CharPos;
import com.jozufozu.flywheel.backend.pipeline.span.ErrorSpan;
import com.jozufozu.flywheel.backend.pipeline.span.Span;
import com.jozufozu.flywheel.backend.pipeline.span.StringSpan;
import com.jozufozu.flywheel.util.StringUtil;

import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import net.minecraft.util.ResourceLocation;

public class SourceFile {
	private static final Pattern includePattern = Pattern.compile("#use \"(.*)\"");

	private static final Pattern newLine = Pattern.compile("(\\r\\n|\\r|\\n)");

	// https://regexr.com/60n3d
	public static final Pattern functionDeclaration = Pattern.compile("(\\w+)\\s+(\\w+)\\s*\\(([\\w,\\s]*)\\)\\s*\\{");

	public final ResourceLocation name;

	private final ShaderSources parent;
	private final String source;
	private final CharSequence elided;
	private final ImmutableList<String> lines;

	private final IntList lineStarts;

	// Function name -> Function object
	private final ImmutableMap<String, ShaderFunction> functions;
	private final ImmutableMap<String, ShaderStruct> structs;

	// Includes ordered as defined in the source
	private final ImmutableList<Include> includes;

	// Sections of the source that must be trimmed for compilation.
	private final List<Span> elisions = new ArrayList<>();

	public SourceFile(ShaderSources parent, ResourceLocation name, String source) {
		this.parent = parent;
		this.name = name;
		this.source = source;

		this.lineStarts = createLineStarts();
		this.lines = createLineList(lineStarts);

		this.includes = parseIncludes();
		this.functions = parseFunctions();
		this.structs = parseStructs();

		this.elided = createElidedSource();
	}

	public String getSource() {
		return source;
	}

	public CharSequence getElidedSource() {
		return elided;
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

	public void resolveIncludes() {
		for (Include include : includes) {
			include.resolve();
		}
	}

	public CharPos getCharPos(int charPos) {
		int lineNo = 0;
		for (; lineNo < lineStarts.size(); lineNo++) {
			int ls = lineStarts.getInt(lineNo);

			if (charPos < ls) {
				break;
			}
		}

		lineNo -= 1;

		int lineStart = lineStarts.getInt(lineNo);

		return new CharPos(charPos, lineNo, charPos - lineStart);
	}

	public String printSource() {
		StringBuilder builder = new StringBuilder();

		builder.append("Source for shader '")
				.append(name)
				.append("':\n");
		int i = 1;
		for (String s : lines) {
			builder.append(String.format("%1$4s: ", i++))
					.append(s)
					.append('\n');
		}

		return builder.toString();
	}

	private CharSequence createElidedSource() {
		StringBuilder out = new StringBuilder();

		int lastEnd = 0;

		for (Span elision : elisions) {
			out.append(source, lastEnd, elision.getStartPos());

			lastEnd = elision.getEndPos();
		}

		out.append(source, lastEnd, source.length());

		return out;
	}

	/**
	 * Scan the source for line breaks, recording the position of the first character of each line.
	 */
	private IntList createLineStarts() {
		IntList l = new IntArrayList();
		l.add(0); // first line is always at position 0

		Matcher matcher = newLine.matcher(source);

		while (matcher.find()) {
			l.add(matcher.end());
		}
		return l;
	}

	private ImmutableList<String> createLineList(IntList lines) {
		ImmutableList.Builder<String> builder = ImmutableList.builder();

		for (int i = 1; i < lines.size(); i++) {
			int start = lines.getInt(i - 1);
			int end = lines.getInt(i);

			builder.add(StringUtil.trimEnd(source.substring(start, end)));
		}

		return builder.build();
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
	 * Scan the source for function definitions and "parse" them into objects that contain properties of the function.
	 */
	private ImmutableMap<String, ShaderStruct> parseStructs() {
		Matcher matcher = ShaderStruct.struct.matcher(source);

		ImmutableMap.Builder<String, ShaderStruct> functions = ImmutableMap.builder();
		while (matcher.find()) {
			Span self = Span.fromMatcher(this, matcher);
			Span name = Span.fromMatcher(this, matcher, 1);
			Span body = Span.fromMatcher(this, matcher, 2);

			ShaderStruct shaderStruct = new ShaderStruct(self, name, body);

			functions.put(body.get(), shaderStruct);
		}

		return functions.build();
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

	public int getLineCount() {
		return lines.size();
	}

	public CharSequence getLine(int lineNo) {
		return lines.get(lineNo);
	}
}
