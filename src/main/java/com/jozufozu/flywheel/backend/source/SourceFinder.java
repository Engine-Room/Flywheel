package com.jozufozu.flywheel.backend.source;

import javax.annotation.Nullable;

import net.minecraft.resources.ResourceLocation;

/**
 * A minimal source file lookup function.
 */
@FunctionalInterface
public interface SourceFinder {

	@Nullable
	SourceFile findSource(ResourceLocation name);
}
