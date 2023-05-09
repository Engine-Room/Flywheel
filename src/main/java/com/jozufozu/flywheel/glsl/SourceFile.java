package com.jozufozu.flywheel.glsl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Matcher;

import org.jetbrains.annotations.NotNull;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.jozufozu.flywheel.glsl.parse.Import;
import com.jozufozu.flywheel.glsl.parse.ShaderField;
import com.jozufozu.flywheel.glsl.parse.ShaderFunction;
import com.jozufozu.flywheel.glsl.parse.ShaderStruct;
import com.jozufozu.flywheel.glsl.span.ErrorSpan;
import com.jozufozu.flywheel.glsl.span.Span;
import com.jozufozu.flywheel.glsl.span.StringSpan;

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

	public final String finalSource;

	private SourceFile(ResourceLocation name, SourceLines source, ImmutableMap<String, ShaderFunction> functions, ImmutableMap<String, ShaderStruct> structs, ImmutableList<Import> imports, ImmutableMap<String, ShaderField> fields, List<SourceFile> included, String finalSource) {
		this.name = name;
		this.source = source;
		this.functions = functions;
		this.structs = structs;
		this.imports = imports;
		this.fields = fields;
		this.included = included;
		this.finalSource = finalSource;
	}

	public static LoadResult parse(ShaderSources sourceFinder, ResourceLocation name, String stringSource) {
		var source = new SourceLines(name, stringSource);

		var imports = parseImports(source);

		List<SourceFile> included = new ArrayList<>();
		List<LoadResult> failures = new ArrayList<>();

		Set<String> seen = new HashSet<>();
		for (Import i : imports) {
			var fileSpan = i.file();
			String string = fileSpan.toString();
			if (!seen.add(string)) {
				continue;
			}
			var result = sourceFinder.find(new ResourceLocation(string));
			if (result instanceof LoadResult.Success s) {
				included.add(s.unwrap());
			} else {
				failures.add(result);
			}
		}
		if (!failures.isEmpty()) {
			return new LoadResult.IncludeError(name, failures);
		}

		var functions = parseFunctions(source);
		var structs = parseStructs(source);
		var fields = parseFields(source);

		var finalSource = generateFinalSource(imports, source);
		return LoadResult.success(new SourceFile(name, source, functions, structs, imports, fields, included, finalSource));
	}

	@Override
	public Collection<? extends SourceComponent> included() {
		return included;
	}

	@Override
	public String source() {
		return finalSource;
	}

	@NotNull
	private static String generateFinalSource(ImmutableList<Import> imports, SourceLines source) {
		var out = new StringBuilder();

		int lastEnd = 0;

		for (var include : imports) {
			var loc = include.self();

			out.append(source, lastEnd, loc.startIndex());

			lastEnd = loc.endIndex();
		}

		out.append(source, lastEnd, source.length());

		return out.toString();
	}

	@Override
	public ResourceLocation name() {
		return name;
	}

	public Span getLineSpan(int lineNo) {
		int begin = source.lineStartIndex(lineNo);
		int end = begin + source.lineString(lineNo)
				.length();
		return new StringSpan(source, begin, end);
	}

	public Span getLineSpanNoWhitespace(int line) {
		int begin = source.lineStartIndex(line);
		int end = begin + source.lineString(line)
				.length();

		while (begin < end && Character.isWhitespace(source.charAt(begin))) {
			begin++;
		}

		return new StringSpan(source, begin, end);
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
	private static ImmutableList<Import> parseImports(SourceLines source) {
		Matcher uses = Import.PATTERN.matcher(source);

		var imports = ImmutableList.<Import>builder();

		while (uses.find()) {
			Span use = Span.fromMatcher(source, uses);
			Span file = Span.fromMatcher(source, uses, 1);

			imports.add(new Import(use, file));
		}

		return imports.build();
	}

	/**
	 * Scan the source for function definitions and "parse" them into objects that contain properties of the function.
	 */
	private static ImmutableMap<String, ShaderFunction> parseFunctions(SourceLines source) {
		Matcher matcher = ShaderFunction.PATTERN.matcher(source);

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
	 * Scan the source for function definitions and "parse" them into objects that contain properties of the function.
	 */
	private static ImmutableMap<String, ShaderStruct> parseStructs(SourceLines source) {
		Matcher matcher = ShaderStruct.PATTERN.matcher(source);

		ImmutableMap.Builder<String, ShaderStruct> structs = ImmutableMap.builder();
		while (matcher.find()) {
			Span self = Span.fromMatcher(source, matcher);
			Span name = Span.fromMatcher(source, matcher, 1);
			Span body = Span.fromMatcher(source, matcher, 2);
			Span variableName = Span.fromMatcher(source, matcher, 3);

			ShaderStruct shaderStruct = new ShaderStruct(self, name, body, variableName);

			structs.put(name.get(), shaderStruct);
		}

		return structs.build();
	}

	/**
	 * Scan the source for function definitions and "parse" them into objects that contain properties of the function.
	 */
	private static ImmutableMap<String, ShaderField> parseFields(SourceLines source) {
		Matcher matcher = ShaderField.PATTERN.matcher(source);

		ImmutableMap.Builder<String, ShaderField> fields = ImmutableMap.builder();
		while (matcher.find()) {
			Span self = Span.fromMatcher(source, matcher);
			Span location = Span.fromMatcher(source, matcher, 1);
			Span decoration = Span.fromMatcher(source, matcher, 2);
			Span type = Span.fromMatcher(source, matcher, 3);
			Span name = Span.fromMatcher(source, matcher, 4);

			fields.put(location.get(), new ShaderField(self, location, decoration, type, name));
		}

		return fields.build();
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
}
