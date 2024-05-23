package dev.engine_room.flywheel.backend.glsl.generate;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import dev.engine_room.flywheel.lib.util.Pair;
import dev.engine_room.flywheel.lib.util.StringUtil;

public class GlslUniformBlock implements GlslBuilder.Declaration {
	private String qualifier;
	private String name;
	private final List<Pair<String, String>> members = new ArrayList<>();

	@Override
	public String prettyPrint() {
		return """
				layout(%s) uniform %s {
				%s
				};""".formatted(qualifier, name, StringUtil.indent(formatMembers(), 4));
	}

	private String formatMembers() {
		return members.stream()
			.map(p -> p.first() + " " + p.second() + ";")
			.collect(Collectors.joining("\n"));
	}

	public GlslUniformBlock layout(String qualifier) {
		this.qualifier = qualifier;
		return this;
	}

	public GlslUniformBlock name(String name) {
		this.name = name;
		return this;
	}

	public GlslUniformBlock member(String typeName, String variableName) {
		members.add(Pair.of(typeName, variableName));
        return this;
	}
}
