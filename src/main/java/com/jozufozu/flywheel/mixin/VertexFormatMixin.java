package com.jozufozu.flywheel.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

import com.jozufozu.flywheel.api.vertex.VertexViewProvider;
import com.jozufozu.flywheel.extension.VertexFormatExtension;
import com.mojang.blaze3d.vertex.VertexFormat;

@Mixin(VertexFormat.class)
abstract class VertexFormatMixin implements VertexFormatExtension {
	@Unique
	private VertexViewProvider flywheel$vertexViewProvider;

	@Override
	public VertexViewProvider flywheel$getVertexViewProvider() {
		return flywheel$vertexViewProvider;
	}

	@Override
	public void flywheel$setVertexViewProvider(VertexViewProvider provider) {
		flywheel$vertexViewProvider = provider;
	}
}
