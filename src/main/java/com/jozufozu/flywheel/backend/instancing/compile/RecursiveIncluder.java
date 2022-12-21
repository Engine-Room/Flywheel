package com.jozufozu.flywheel.backend.instancing.compile;

import java.util.LinkedHashSet;
import java.util.Set;
import java.util.function.Consumer;

import com.google.common.collect.ImmutableList;
import com.jozufozu.flywheel.core.SourceComponent;

public class RecursiveIncluder implements Includer {

	public static final RecursiveIncluder INSTANCE = new RecursiveIncluder();

	private RecursiveIncluder() {
	}

	@Override
	public void expand(ImmutableList<SourceComponent> rootSources, Consumer<SourceComponent> out) {
		var included = depthFirstInclude(rootSources);
		included.forEach(out);
		rootSources.forEach(out);
	}

	private static LinkedHashSet<SourceComponent> depthFirstInclude(ImmutableList<SourceComponent> root) {
		var included = new LinkedHashSet<SourceComponent>(); // linked to preserve order
		for (var component : root) {
			recursiveDepthFirstInclude(included, component);
		}
		return included;
	}

	private static void recursiveDepthFirstInclude(Set<SourceComponent> included, SourceComponent component) {
		for (var include : component.included()) {
			recursiveDepthFirstInclude(included, include);
		}
		included.addAll(component.included());
	}
}
