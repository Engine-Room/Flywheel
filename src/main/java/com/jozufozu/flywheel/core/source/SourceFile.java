package com.jozufozu.flywheel.core.source;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Matcher;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.jozufozu.flywheel.core.source.error.ErrorReporter;
import com.jozufozu.flywheel.core.source.parse.Import;
import com.jozufozu.flywheel.core.source.parse.ShaderFunction;
import com.jozufozu.flywheel.core.source.parse.ShaderStruct;
import com.jozufozu.flywheel.core.source.span.ErrorSpan;
import com.jozufozu.flywheel.core.source.span.Span;
import com.jozufozu.flywheel.core.source.span.StringSpan;
import com.jozufozu.flywheel.util.Pair;

import net.minecraft.resources.ResourceLocation;

/**
 * Immutable class representing a shader file.
 *
 * <p>
 *     This class parses shader files and generates what is effectively a high level AST of the source.
 * </p>
 */
public class SourceFile {

	public final ResourceLocation name;

	public final ShaderSources parent;
	public final String source;

	public final SourceLines lines;

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
	private final List<Span> elisions;

	public SourceFile(ErrorReporter errorReporter, ShaderSources parent, ResourceLocation name, String source) {
		this.parent = parent;
		this.name = name;
		this.source = source;

		this.lines = new SourceLines(source);

		this.elisions = new ArrayList<>();

		this.imports = parseImports(errorReporter);
		this.functions = parseFunctions();
		this.structs = parseStructs();
		this.fields = parseFields();
	}

	public Span getLineSpan(int line) {
		int begin = lines.getLineStart(line);
		int end = begin + lines.getLine(line).length();
		return new StringSpan(this, lines.getCharPos(begin), lines.getCharPos(end));
	}

	public Span getLineSpanNoWhitespace(int line) {
		int begin = lines.getLineStart(line);
		int end = begin + lines.getLine(line).length();

		while (begin < end && Character.isWhitespace(source.charAt(begin))) {
			begin++;
		}

		return new StringSpan(this, lines.getCharPos(begin), lines.getCharPos(end));
	}

	/**
	 * Search this file and recursively search all imports to find a struct definition matching the given name.
	 *
	 * @param name The name of the struct to find.
	 * @return null if no definition matches the name.
	 */
	public Optional<ShaderStruct> findStruct(CharSequence name) {
		ShaderStruct struct = structs.get(name.toString());

		if (struct != null) return Optional.of(struct);

		for (Import include : imports) {
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
		ShaderFunction local = functions.get(name.toString());

		if (local != null) return Optional.of(local);

		for (Import include : imports) {
			Optional<ShaderFunction> external = include.getOptional()
					.flatMap(file -> file.findFunction(name));

			if (external.isPresent()) return external;
		}

		return Optional.empty();
	}

	public CharSequence importStatement() {
		return "#use " + '"' + name + '"';
	}

	public void generateFinalSource(FileIndex env, StringBuilder source) {
		generateFinalSource(env, source, Collections.emptyList());
	}

	public void generateFinalSource(FileIndex env, StringBuilder source, List<Pair<Span, String>> replacements) {
		for (Import include : imports) {
			SourceFile file = include.getFile();

			if (file != null && !env.exists(file)) {
				file.generateFinalSource(env, source, replacements);
			}
		}

		source.append("#line ")
				.append(0)
				.append(' ')
				.append(env.getFileID(this))
				.append(" // ")
				.append(name)
				.append('\n');

		var replacementsAndElisions = new ArrayList<>(replacements);
		for (Span elision : elisions) {
			replacementsAndElisions.add(Pair.of(elision, ""));
		}

		source.append(this.replace(replacementsAndElisions));
		source.append('\n');
	}

	public String printSource() {
		return "Source for shader '" + name + "':\n" + lines.printLinesWithNumbers();
	}

	private CharSequence replace(List<Pair<Span, String>> replacements) {
		StringBuilder out = new StringBuilder();

		int lastEnd = 0;

		replacements.sort(Comparator.comparing(Pair::first));

		for (var replacement : replacements) {
			var loc = replacement.first();

			if (loc.getSourceFile() != this) {
				continue;
			}

			out.append(this.source, lastEnd, loc.getStartPos());
			out.append(replacement.second());

			lastEnd = loc.getEndPos();
		}

		out.append(this.source, lastEnd, this.source.length());

		return out;
	}

	/**
	 * Scan the source for function definitions and "parse" them into objects that contain properties of the function.
	 */
	private ImmutableMap<String, ShaderFunction> parseFunctions() {
		Matcher matcher = ShaderFunction.PATTERN.matcher(source);

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
		Matcher matcher = ShaderStruct.PATTERN.matcher(source);

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
	 * Scan the source for function definitions and "parse" them into objects that contain properties of the function.
	 */
	private ImmutableMap<String, ShaderField> parseFields() {
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
	 * Scan the source for {@code #use "..."} directives.
	 * Records the contents of the directive into an {@link Import} object, and marks the directive for elision.
	 */
	private ImmutableList<Import> parseImports(ErrorReporter errorReporter) {
		Matcher uses = Import.PATTERN.matcher(source);

		Set<String> importedFiles = new HashSet<>();
		List<Import> imports = new ArrayList<>();

		while (uses.find()) {
			Span use = Span.fromMatcher(this, uses);
			Span file = Span.fromMatcher(this, uses, 1);

			String fileName = file.get();
			if (importedFiles.add(fileName)) {
				// FIXME: creating imports after the first resource reload crashes the game
				var checked = Import.create(errorReporter, use, file);
				if (checked != null) {
					imports.add(checked);
				}
			}

			this.elisions.add(use); // we have to trim that later
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
}
