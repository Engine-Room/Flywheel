package com.jozufozu.flywheel.mixin.fabric;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.vertex.VertexFormat;

@Mixin(BufferBuilder.class)
public interface BufferBuilderAccessor {
	@Accessor("vertexFormat")
	VertexFormat getVertexFormat();
}
