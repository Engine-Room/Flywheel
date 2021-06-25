package com.jozufozu.flywheel.backend.pipeline;

import com.jozufozu.flywheel.backend.loading.LayoutTag;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class StructField {
	public static final Pattern fieldPattern = Pattern.compile("(\\S+)\\s*(\\S+);");

	public String name;
	public String type;
	public LayoutTag layout;

	public StructField(Matcher fieldMatcher) {
		type = fieldMatcher.group(2);
		name = fieldMatcher.group(3);
	}

	public String getName() {
		return name;
	}

	public String getType() {
		return type;
	}

	@Override
	public String toString() {
		return "TaggedField{" +
				"name='" + name + '\'' +
				", type='" + type + '\'' +
				'}';
	}
}
