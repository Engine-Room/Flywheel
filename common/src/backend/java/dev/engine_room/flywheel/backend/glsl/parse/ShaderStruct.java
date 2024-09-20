package dev.engine_room.flywheel.backend.glsl.parse;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;

import dev.engine_room.flywheel.backend.glsl.SourceLines;
import dev.engine_room.flywheel.backend.glsl.span.Span;

public class ShaderStruct {
	// https://regexr.com/61rpe
	public static final Pattern PATTERN = Pattern.compile("struct\\s+([\\w_]*)\\s*\\{(.*?)}\\s*([\\w_]*)?\\s*;\\s", Pattern.DOTALL);

	public final Span self;
	public final Span name;
	public final Span body;
	public final Span variableName;

	public final ImmutableList<StructField> fields;
	public final ImmutableMap<String, Span> fields2Types;

	public ShaderStruct(Span self, Span name, Span body, Span variableName) {
		this.self = self;
		this.name = name;
		this.body = body;
		this.variableName = variableName;

		this.fields = parseFields();
		this.fields2Types = createTypeLookup();
	}

	/**
	 * Scan the source for function definitions and "parse" them into objects that contain properties of the function.
	 */
	public static ImmutableMap<String, ShaderStruct> parseStructs(SourceLines source) {
		Matcher matcher = PATTERN.matcher(source);

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

	private ImmutableList<StructField> parseFields() {
		Matcher matcher = StructField.PATTERN.matcher(body);

		ImmutableList.Builder<StructField> fields = ImmutableList.builder();

		while (matcher.find()) {
			Span field = Span.fromMatcher(body, matcher);
			Span type = Span.fromMatcher(body, matcher, 1);
			Span name = Span.fromMatcher(body, matcher, 2);

			fields.add(new StructField(field, type, name));
		}

		return fields.build();
	}

	private ImmutableMap<String, Span> createTypeLookup() {
		ImmutableMap.Builder<String, Span> lookup = ImmutableMap.builder();
		for (StructField field : fields) {
			lookup.put(field.name.get(), field.type);
		}

		return lookup.build();
	}

	@Override
	public String toString() {
		return "struct " + name;
	}
}
