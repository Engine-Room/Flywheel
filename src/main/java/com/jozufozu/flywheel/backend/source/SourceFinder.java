package com.jozufozu.flywheel.backend.source;

import net.minecraft.resources.ResourceLocation;

/**
 * A minimal source file lookup function.
 */
@FunctionalInterface
public interface SourceFinder {

	SourceFile findSource(ResourceLocation name);
}
