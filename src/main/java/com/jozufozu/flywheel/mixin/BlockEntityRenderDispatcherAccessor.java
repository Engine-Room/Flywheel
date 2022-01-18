package com.jozufozu.flywheel.mixin;

import java.util.Map;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import net.minecraft.client.renderer.blockentity.BlockEntityRenderDispatcher;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.world.level.block.entity.BlockEntityType;

@Mixin(BlockEntityRenderDispatcher.class)
public interface BlockEntityRenderDispatcherAccessor {
	@Accessor("renderers")
	Map<BlockEntityType<?>, BlockEntityRenderer<?>> flywheel$getRenderers();
}
