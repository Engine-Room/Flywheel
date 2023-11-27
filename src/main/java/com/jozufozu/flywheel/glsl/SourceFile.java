package com.jozufozu.flywheel.glsl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.jozufozu.flywheel.glsl.parse.Import;
import com.jozufozu.flywheel.glsl.parse.ShaderField;
import com.jozufozu.flywheel.glsl.parse.ShaderFunction;
import com.jozufozu.flywheel.glsl.parse.ShaderStruct;
import com.jozufozu.flywheel.glsl.span.Span;
import com.jozufozu.flywheel.glsl.span.StringSpan;
import com.jozufozu.flywheel.lib.util.Pair;
import com.jozufozu.flywheel.lib.util.ResourceUtil;

import net.minecraft.ResourceLocationException;
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

		var imports = Import.parseImports(source);

		List<SourceFile> included = new ArrayList<>();
		List<Pair<Span, LoadError>> failures = new ArrayList<>();

		Set<String> seen = new HashSet<>();
		for (Import i : imports) {
			var fileSpan = i.file();
			String string = fileSpan.toString();
			if (!seen.add(string)) {
				continue;
			}

			ResourceLocation location;
			try {
				location = ResourceUtil.defaultToFlywheelNamespace(string);
			} catch (ResourceLocationException e) {
				failures.add(Pair.of(fileSpan, new LoadError.MalformedInclude(e)));
				continue;
			}

			var result = sourceFinder.find(location);

			if (result instanceof LoadResult.Success s) {
				included.add(s.unwrap());
			} else if (result instanceof LoadResult.Failure e) {
				failures.add(Pair.of(fileSpan, e.error()));
			}
		}
		if (!failures.isEmpty()) {
			return new LoadResult.Failure(new LoadError.IncludeError(name, failures));
		}

		var functions = ShaderFunction.parseFunctions(source);
		var structs = ShaderStruct.parseStructs(source);
		var fields = ShaderField.parseFields(source);

		var finalSource = generateFinalSource(imports, source);
		return new LoadResult.Success(new SourceFile(name, source, functions, structs, imports, fields, included, finalSource));
	}

	@Override
	public Collection<? extends SourceComponent> included() {
		return included;
	}

	@Override
	public String source() {
		return finalSource;
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

	public Span getLineSpanMatching(int line, @Nullable String match) {
		if (match == null) {
			return getLineSpanNoWhitespace(line);
		}

		var spanBegin = source.lineString(line)
				.indexOf(match);

		if (spanBegin == -1) {
			return getLineSpanNoWhitespace(line);
		}

		int begin = source.lineStartIndex(line) + spanBegin;
		int end = begin + match.length();

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
}
