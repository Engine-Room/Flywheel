package com.jozufozu.flywheel.mixin.fabric;

import com.mojang.blaze3d.vertex.BufferBuilder;

import com.mojang.blaze3d.vertex.VertexFormat;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;


@Mixin(BufferBuilder.class)
public interface BufferBuilderAccessor {
	@Accessor("format")
	VertexFormat getVertexFormat();
}
