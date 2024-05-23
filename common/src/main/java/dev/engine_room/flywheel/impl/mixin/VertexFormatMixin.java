package dev.engine_room.flywheel.impl.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

import com.mojang.blaze3d.vertex.VertexFormat;

import dev.engine_room.flywheel.api.vertex.VertexViewProvider;
import dev.engine_room.flywheel.impl.extension.VertexFormatExtension;

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
