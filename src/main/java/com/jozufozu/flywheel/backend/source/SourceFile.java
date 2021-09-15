package com.jozufozu.flywheel.backend.source;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.jozufozu.flywheel.backend.source.parse.Import;
import com.jozufozu.flywheel.backend.source.parse.ShaderFunction;
import com.jozufozu.flywheel.backend.source.parse.ShaderStruct;
import com.jozufozu.flywheel.backend.source.span.CharPos;
import com.jozufozu.flywheel.backend.source.span.ErrorSpan;
import com.jozufozu.flywheel.backend.source.span.Span;
import com.jozufozu.flywheel.backend.source.span.StringSpan;
import com.jozufozu.flywheel.util.StringUtil;

import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import net.minecraft.resources.ResourceLocation;

public class SourceFile {
	private static final Pattern includePattern = Pattern.compile("#use \"(.*)\"");

	private static final Pattern newLine = Pattern.compile("(\\r\\n|\\r|\\n)");

	// https://regexr.com/60n3d
	public static final Pattern functionDeclaration = Pattern.compile("(\\w+)\\s+(\\w+)\\s*\\(([\\w,\\s]*)\\)\\s*\\{");

	public final ResourceLocation name;

	public final ShaderSources parent;
	private final String source;
	private final CharSequence elided;
	private final ImmutableList<String> lines;

	private final IntList lineStarts;

	// Function name -> Function object
	private final ImmutableMap<String, ShaderFunction> functions;
	private final ImmutableMap<String, ShaderStruct> structs;

	// Includes ordered as defined in the source
	private final ImmutableList<Import> imports;

	// Sections of the source that must be trimmed for compilation.
	private final List<Span> elisions = new ArrayList<>();

	public SourceFile(ShaderSources parent, ResourceLocation name, String source) {
		this.parent = parent;
		this.name = name;
		this.source = source;

		this.lineStarts = createLineStarts();
		this.lines = createLineList(lineStarts);

		this.imports = parseImports();
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

	public ImmutableMap<String, ShaderStruct> getStructs() {
		return structs;
	}

	public ImmutableList<Import> getIncludes() {
		return imports;
	}

	/**
	 * Search this file and recursively search all imports to find a struct definition matching the given name.
	 *
	 * @param name The name of the struct to find.
	 * @return null if no definition matches the name.
	 */
	public Optional<ShaderStruct> findStruct(CharSequence name) {
		ShaderStruct struct = getStructs().get(name.toString());

		if (struct != null) return Optional.of(struct);

		for (Import include : getIncludes()) {
			Optional<ShaderStruct> externalStruct = include.getOptional()
					.flatMap(file -> file.findStruct(name));

			if (externalStruct.isPresent()) return externalStruct;
		}

		return Optional.empty();
	}

	/**
	 * Search this file and recursively search all imports to find a function definition matching the given name.
	 *
	 * @param name The name of the function to find.
	 * @return Optional#empty() if no definition matches the name.
	 */
	public Optional<ShaderFunction> findFunction(CharSequence name) {
		ShaderFunction local = getFunctions().get(name.toString());

		if (local != null) return Optional.of(local);

		for (Import include : getIncludes()) {
			Optional<ShaderFunction> external = include.getOptional()
					.flatMap(file -> file.findFunction(name));

			if (external.isPresent()) return external;
		}

		return Optional.empty();
	}

	public CharSequence importStatement() {
		return "#use " + '"' + name + '"';
	}

	public CharSequence generateFinalSource() {
		StringBuilder builder = new StringBuilder();
		generateFinalSource(builder);
		return builder;
	}

	public void generateFinalSource(StringBuilder source) {
		for (Import include : getIncludes()) {
			SourceFile file = include.getFile();

			if (file != null) file.generateFinalSource(source);
		}
		source.append(getElidedSource());
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

		ImmutableMap.Builder<String, ShaderStruct> structs = ImmutableMap.builder();
		while (matcher.find()) {
			Span self = Span.fromMatcher(this, matcher);
			Span name = Span.fromMatcher(this, matcher, 1);
			Span body = Span.fromMatcher(this, matcher, 2);

			ShaderStruct shaderStruct = new ShaderStruct(self, name, body);

			structs.put(name.get(), shaderStruct);
		}

		return structs.build();
	}

	/**
	 * Scan the source for <code>#use "..."</code> directives.
	 * Records the contents of the directive into an {@link Import} object, and marks the directive for elision.
	 */
	private ImmutableList<Import> parseImports() {
		Matcher uses = includePattern.matcher(source);

		List<Import> imports = new ArrayList<>();

		while (uses.find()) {
			Span use = Span.fromMatcher(this, uses);
			Span file = Span.fromMatcher(this, uses, 1);

			imports.add(new Import(Resolver.INSTANCE, use, file));

			elisions.add(use); // we have to trim that later
		}

		return ImmutableList.copyOf(imports);
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
