package dev.engine_room.flywheel.impl.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import com.mojang.blaze3d.vertex.BufferBuilder;

@Mixin(BufferBuilder.class)
public interface BufferBuilderAccessor {
	@Accessor("building")
	boolean flywheel$getBuilding();
}
