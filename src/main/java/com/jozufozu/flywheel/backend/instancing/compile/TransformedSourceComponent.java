package com.jozufozu.flywheel.backend.instancing.compile;

import java.util.Collection;

import com.jozufozu.flywheel.core.SourceComponent;
import com.jozufozu.flywheel.core.source.CompilationContext;
import com.jozufozu.flywheel.util.ResourceUtil;

import net.minecraft.resources.ResourceLocation;

public final class TransformedSourceComponent implements SourceComponent {
	final SourceComponent source;
	final String find;
	final String replacement;

	public TransformedSourceComponent(SourceComponent source, String find, String replacement) {
		this.source = source;
		this.find = find;
		this.replacement = replacement;
	}

	@Override
	public String source(CompilationContext ctx) {
		return source.source(ctx)
				.replace(find, replacement);
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
