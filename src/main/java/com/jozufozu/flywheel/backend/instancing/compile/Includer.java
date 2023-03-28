package com.jozufozu.flywheel.backend.instancing.compile;

import java.util.function.Consumer;

import com.google.common.collect.ImmutableList;
import com.jozufozu.flywheel.core.SourceComponent;

/**
 * A component of a ShaderCompiler, responsible for expanding root sources into the complete set of included sources.
 */
public interface Includer {

	/**
	 * Expand the given root sources into the complete set of included sources.
	 * <p> Each unique source will be seen exactly once.
	 *
	 * @param rootSources The root sources to expand.
	 * @param out         A consumer to which all sources should be passed in the order they should be included.
	 */
	void expand(ImmutableList<SourceComponent> rootSources, Consumer<SourceComponent> out);
}
