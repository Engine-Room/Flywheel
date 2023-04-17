package com.jozufozu.flywheel.glsl.generate;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import com.jozufozu.flywheel.util.Pair;
import com.jozufozu.flywheel.util.StringUtil;

public class GlslStruct implements GlslBuilder.Declaration {

	private final List<Pair<String, String>> fields = new ArrayList<>();
	private String name;

	public void setName(String name) {
		this.name = name;
	}

	public void addField(String type, String name) {
		fields.add(Pair.of(type, name));
	}

	private String buildFields() {
		return fields.stream()
				.map(p -> p.first() + ' ' + p.second() + ';')
				.collect(Collectors.joining("\n"));
	}

	@Override
	public String prettyPrint() {
		return """
				struct %s {
				%s
				};""".formatted(name, StringUtil.indent(buildFields(), 4));
	}
}
