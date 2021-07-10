package com.jozufozu.flywheel.mixin.fabric;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import it.unimi.dsi.fastutil.ints.IntList;
import net.minecraft.client.renderer.vertex.VertexFormat;

@Mixin(VertexFormat.class)
public interface VertexFormatAccessor {
	@Accessor("offsets")
	IntList getOffsets();
}
