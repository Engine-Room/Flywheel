package com.jozufozu.flywheel.core.source.parse;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.jozufozu.flywheel.core.source.span.Span;

public class ShaderStruct extends AbstractShaderElement {

	// https://regexr.com/61rpe
	public static final Pattern struct = Pattern.compile("struct\\s+([\\w\\d]*)\\s*\\{([\\w\\d\\s,;]*)}\\s*;\\s");

	public final Span name;
	public final Span body;

	private final ImmutableList<StructField> fields;
	private final ImmutableMap<String, Span> fields2Types;

	public ShaderStruct(Span self, Span name, Span body) {
		super(self);
		this.name = name;
		this.body = body;
		this.fields = parseFields();
		this.fields2Types = createTypeLookup();
	}

	public Span getName() {
		return name;
	}

	public Span getBody() {
		return body;
	}

	public ImmutableList<StructField> getFields() {
		return fields;
	}

	private ImmutableMap<String, Span> createTypeLookup() {
		ImmutableMap.Builder<String, Span> lookup = ImmutableMap.builder();
		for (StructField field : fields) {
			lookup.put(field.name.get(), field.type);
		}

		return lookup.build();
	}

	private ImmutableList<StructField> parseFields() {
		Matcher matcher = StructField.fieldPattern.matcher(body);

		ImmutableList.Builder<StructField> fields = ImmutableList.builder();

		while (matcher.find()) {
			Span field = Span.fromMatcher(body, matcher);
			Span type = Span.fromMatcher(body, matcher, 1);
			Span name = Span.fromMatcher(body, matcher, 2);

			fields.add(new StructField(field, type, name));
		}

		return fields.build();
	}

	@Override
	public String toString() {
		return "struct " + name;
	}
}
