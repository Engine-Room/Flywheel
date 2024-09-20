package dev.engine_room.flywheel.backend.glsl.generate;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import com.mojang.datafixers.util.Pair;

import dev.engine_room.flywheel.lib.util.StringUtil;

public class GlslStruct implements GlslBuilder.Declaration {
	private String name;
	private final List<Pair<String, String>> fields = new ArrayList<>();

	public GlslStruct name(String name) {
		this.name = name;
		return this;
	}

	public GlslStruct addField(String type, String name) {
		fields.add(Pair.of(type, name));
		return this;
	}

	@Override
	public String prettyPrint() {
		return """
				struct %s {
				%s
				};""".formatted(name, StringUtil.indent(buildFields(), 4));
	}

	private String buildFields() {
		return fields.stream()
				.map(p -> p.getFirst() + ' ' + p.getSecond() + ';')
				.collect(Collectors.joining("\n"));
	}
}
