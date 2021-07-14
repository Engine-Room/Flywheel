package com.jozufozu.flywheel.backend.pipeline.parse;

import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.jozufozu.flywheel.backend.loading.Program;
import com.jozufozu.flywheel.backend.loading.TypeHelper;
import com.jozufozu.flywheel.backend.pipeline.error.ErrorReporter;
import com.jozufozu.flywheel.backend.pipeline.span.Span;

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

	public void addPrefixedAttributes(Program builder, String prefix) {
		for (StructField field : fields) {
			int attributeCount = TypeHelper.getAttributeCount(field.type);

			builder.addAttribute(prefix + field.name, attributeCount);
		}
	}
}
