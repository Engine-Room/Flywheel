package com.jozufozu.flywheel.core.source;

import org.jetbrains.annotations.Nullable;

import net.minecraft.resources.ResourceLocation;

/**
 * A minimal source file lookup function.
 */
@FunctionalInterface
public interface SourceFinder {

	@Nullable
	SourceFile findSource(ResourceLocation name);
}
