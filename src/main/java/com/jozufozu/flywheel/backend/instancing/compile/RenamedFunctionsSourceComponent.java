package com.jozufozu.flywheel.backend.instancing.compile;

import java.util.Collection;
import java.util.Map;

import com.jozufozu.flywheel.core.SourceComponent;
import com.jozufozu.flywheel.util.ResourceUtil;

import net.minecraft.resources.ResourceLocation;

public final class RenamedFunctionsSourceComponent implements SourceComponent {
	private final SourceComponent source;
	private final Map<String, String> replacements;
	private final String sourceString;

	public RenamedFunctionsSourceComponent(SourceComponent source, String find, String replace) {
		this(source, Map.of(find, replace));
	}

	public RenamedFunctionsSourceComponent(SourceComponent source, Map<String, String> replacements) {
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
		return ResourceUtil.subPath(source.name(), "_renamed");
	}

	@Override
	public Collection<? extends SourceComponent> included() {
		return source.included();
	}
}
