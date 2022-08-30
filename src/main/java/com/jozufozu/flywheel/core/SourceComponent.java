package com.jozufozu.flywheel.core;

import java.util.Collection;

import com.jozufozu.flywheel.core.source.CompilationContext;

import net.minecraft.resources.ResourceLocation;

public interface SourceComponent {
	Collection<? extends SourceComponent> included();

	String source(CompilationContext ctx);

	ResourceLocation name();
}
