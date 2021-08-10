package com.jozufozu.flywheel.backend.source;

import net.minecraft.util.ResourceLocation;

/**
 * A minimal source file lookup function.
 */
@FunctionalInterface
public interface ISourceHolder {

	SourceFile findSource(ResourceLocation name);
}
