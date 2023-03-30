package com.jozufozu.flywheel.backend.compile;

import java.util.LinkedHashSet;
import java.util.Set;
import java.util.function.Consumer;

import com.google.common.collect.ImmutableList;
import com.jozufozu.flywheel.api.pipeline.SourceComponent;

public class RecursiveIncluder implements Includer {

	public static final RecursiveIncluder INSTANCE = new RecursiveIncluder();

	private RecursiveIncluder() {
	}

	@Override
	public void expand(ImmutableList<SourceComponent> rootSources, Consumer<SourceComponent> out) {
        var included = new LinkedHashSet<SourceComponent>(); // use hash set to deduplicate. linked to preserve order
        for (var component : rootSources) {
            recursiveDepthFirstInclude(included, component);
            included.add(component);
        }
        included.forEach(out);
    }

	private static void recursiveDepthFirstInclude(Set<SourceComponent> included, SourceComponent component) {
		for (var include : component.included()) {
			recursiveDepthFirstInclude(included, include);
		}
		included.addAll(component.included());
	}
}
