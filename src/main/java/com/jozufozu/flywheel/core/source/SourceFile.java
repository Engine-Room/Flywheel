package com.jozufozu.flywheel.core.source;

import java.util.*;
import java.util.regex.Matcher;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.jozufozu.flywheel.core.SourceComponent;
import com.jozufozu.flywheel.core.source.parse.Import;
import com.jozufozu.flywheel.core.source.parse.ShaderField;
import com.jozufozu.flywheel.core.source.parse.ShaderFunction;
import com.jozufozu.flywheel.core.source.parse.ShaderStruct;
import com.jozufozu.flywheel.core.source.span.ErrorSpan;
import com.jozufozu.flywheel.core.source.span.Span;
import com.jozufozu.flywheel.core.source.span.StringSpan;

import net.minecraft.resources.ResourceLocation;

/**
 * Immutable class representing a shader file.
 *
 * <p>
 * This class parses shader files and generates what is effectively a high level AST of the source.
 * </p>
 */
public class SourceFile implements SourceComponent {

	public final ResourceLocation name;

	public final ShaderSources parent;

	public final SourceLines source;

	/**
	 * Function lookup by name.
	 */
	public final ImmutableMap<String, ShaderFunction> functions;

	/**
	 * Struct lookup by name.
	 */
	public final ImmutableMap<String, ShaderStruct> structs;

	/**
	 * Includes ordered as defined in the source.
	 */
	public final ImmutableList<Import> imports;
	public final ImmutableMap<String, ShaderField> fields;

	public final List<SourceFile> included;

	public SourceFile(ShaderSources sourceFinder, ResourceLocation name, String source) {
		this.parent = sourceFinder;
		this.name = name;

		this.source = new SourceLines(source);

		this.imports = parseImports(source);
		this.functions = parseFunctions(source);
		this.structs = parseStructs(source);
		this.fields = parseFields(source);

		this.included = imports.stream()
				.map(i -> i.file)
				.map(Span::toString)
				.distinct()
				.<SourceFile>mapMulti((file, sink) -> {
					try {
						var loc = new ResourceLocation(file);
						var sourceFile = sourceFinder.find(loc);
						sink.accept(sourceFile);
					} catch (Exception ignored) {
					}
				})
				.toList();
	}

	@Override
	public Collection<? extends SourceComponent> included() {
		return included;
	}

	@Override
	public String source() {
		return this.genFinalSource();
	}

	@Override
	public ResourceLocation name() {
		return name;
	}

	public Span getLineSpan(int lineNo) {
		int begin = source.lineStartIndex(lineNo);
		int end = begin + source.lineString(lineNo)
				.length();
		return new StringSpan(this, source.getCharPos(begin), source.getCharPos(end));
	}

	public Span getLineSpanNoWhitespace(int line) {
		int begin = source.lineStartIndex(line);
		int end = begin + source.lineString(line)
				.length();

		while (begin < end && Character.isWhitespace(source.charAt(begin))) {
			begin++;
		}

		return new StringSpan(this, source.getCharPos(begin), source.getCharPos(end));
	}

	/**
	 * Search this file and recursively search all imports to find a struct definition matching the given name.
	 *
	 * @param name The name of the struct to find.
	 * @return null if no definition matches the name.
	 */
	public Optional<ShaderStruct> findStructByName(String name) {
		ShaderStruct struct = structs.get(name);

		if (struct != null) {
			return Optional.of(struct);
		}

		for (var include : included) {
			var external = include.structs.get(name);

			if (external != null) {
				return Optional.of(external);
			}
		}

		return Optional.empty();
	}

	/**
	 * Search this file and recursively search all imports to find a function definition matching the given name.
	 *
	 * @param name The name of the function to find.
	 * @return Optional#empty() if no definition matches the name.
	 */
	public Optional<ShaderFunction> findFunction(String name) {
		ShaderFunction local = functions.get(name);

		if (local != null) return Optional.of(local);

		for (var include : included) {
			var external = include.functions.get(name);

			if (external != null) {
				return Optional.of(external);
			}
		}

		return Optional.empty();
	}

	public CharSequence importStatement() {
		return "#use " + '"' + name + '"';
	}

	public String printSource() {
		return "Source for shader '" + name + "':\n" + source.printLinesWithNumbers();
	}

	private String genFinalSource() {
		StringBuilder out = new StringBuilder();

		int lastEnd = 0;

		for (var include : imports) {
			var loc = include.self;

			out.append(this.source, lastEnd, loc.getStartPos());

			lastEnd = loc.getEndPos();
		}

		out.append(this.source, lastEnd, this.source.length());

		return out.toString();
	}

	@Override
	public String toString() {
		return name.toString();
	}

	@Override
	public boolean equals(Object o) {
		// SourceFiles are only equal by reference.
		return this == o;
	}

	@Override
	public int hashCode() {
		return System.identityHashCode(this);
	}


	/**
	 * Scan the source for {@code #use "..."} directives.
	 * Records the contents of the directive into an {@link Import} object, and marks the directive for elision.
	 */
	private ImmutableList<Import> parseImports(String source) {
		Matcher uses = Import.PATTERN.matcher(source);

		var imports = ImmutableList.<Import>builder();

		while (uses.find()) {
			Span use = Span.fromMatcher(this, uses);
			Span file = Span.fromMatcher(this, uses, 1);

			imports.add(new Import(use, file));
		}

		return imports.build();
	}


	/**
	 * Scan the source for function definitions and "parse" them into objects that contain properties of the function.
	 */
	private ImmutableMap<String, ShaderFunction> parseFunctions(String source) {
		Matcher matcher = ShaderFunction.PATTERN.matcher(source);

		Map<String, ShaderFunction> functions = new HashMap<>();

		while (matcher.find()) {
			Span type = Span.fromMatcher(this, matcher, 1);
			Span name = Span.fromMatcher(this, matcher, 2);
			Span args = Span.fromMatcher(this, matcher, 3);

			int blockStart = matcher.end();
			int blockEnd = findEndOfBlock(source, blockStart);

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
	private ImmutableMap<String, ShaderStruct> parseStructs(String source) {
		Matcher matcher = ShaderStruct.PATTERN.matcher(source);

		ImmutableMap.Builder<String, ShaderStruct> structs = ImmutableMap.builder();
		while (matcher.find()) {
			Span self = Span.fromMatcher(this, matcher);
			Span name = Span.fromMatcher(this, matcher, 1);
			Span body = Span.fromMatcher(this, matcher, 2);
			Span variableName = Span.fromMatcher(this, matcher, 3);

			ShaderStruct shaderStruct = new ShaderStruct(self, name, body, variableName);

			structs.put(name.get(), shaderStruct);
		}

		return structs.build();
	}

	/**
	 * Scan the source for function definitions and "parse" them into objects that contain properties of the function.
	 */
	private ImmutableMap<String, ShaderField> parseFields(String source) {
		Matcher matcher = ShaderField.PATTERN.matcher(source);

		ImmutableMap.Builder<String, ShaderField> fields = ImmutableMap.builder();
		while (matcher.find()) {
			Span self = Span.fromMatcher(this, matcher);
			Span location = Span.fromMatcher(this, matcher, 1);
			Span decoration = Span.fromMatcher(this, matcher, 2);
			Span type = Span.fromMatcher(this, matcher, 3);
			Span name = Span.fromMatcher(this, matcher, 4);

			fields.put(location.get(), new ShaderField(self, location, decoration, type, name));
		}

		return fields.build();
	}

	/**
	 * Given the position of an opening brace, scans through the source for a paired closing brace.
	 */
	private static int findEndOfBlock(String source, int start) {
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
