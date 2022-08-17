package com.jozufozu.flywheel.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

import com.jozufozu.flywheel.api.vertex.VertexListProvider;
import com.jozufozu.flywheel.extension.VertexFormatExtension;
import com.mojang.blaze3d.vertex.VertexFormat;

@Mixin(VertexFormat.class)
public class VertexFormatMixin implements VertexFormatExtension {
	@Unique
	private VertexListProvider flywheel$vertexListProvider;

	@Override
	public VertexListProvider flywheel$getVertexListProvider() {
		return flywheel$vertexListProvider;
	}

	@Override
	public void flywheel$setVertexListProvider(VertexListProvider provider) {
		flywheel$vertexListProvider = provider;
	}
}
