package com.jozufozu.flywheel.backend.compile.component;

import java.util.Collection;
import java.util.Map;

import com.jozufozu.flywheel.glsl.SourceComponent;
import com.jozufozu.flywheel.util.ResourceUtil;

import net.minecraft.resources.ResourceLocation;

public final class StringSubstitutionSourceComponent implements SourceComponent {
	private final SourceComponent source;
	private final Map<String, String> replacements;
	private final String sourceString;

	public StringSubstitutionSourceComponent(SourceComponent source, String find, String replace) {
		this(source, Map.of(find, replace));
	}

	public StringSubstitutionSourceComponent(SourceComponent source, Map<String, String> replacements) {
		this.source = source;
		this.replacements = replacements;
		this.sourceString = source.source();
	}

	public String remapFnName(String name) {
		return replacements.getOrDefault(name, name);
	}

	public boolean replaces(String name) {
		return replacements.containsKey(name) && sourceString.contains(name);
	}

	@Override
	public String source() {
		var source = sourceString;

		for (var entry : replacements.entrySet()) {
			source = source.replace(entry.getKey(), entry.getValue());
		}

		return source;
	}

	@Override
	public ResourceLocation name() {
		return ResourceUtil.subPath(source.name(), "_string_substitution");
	}

	@Override
	public Collection<? extends SourceComponent> included() {
		return source.included();
	}
}
