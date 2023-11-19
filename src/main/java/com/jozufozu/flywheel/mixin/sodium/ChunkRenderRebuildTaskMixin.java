package com.jozufozu.flywheel.mixin.sodium;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import com.jozufozu.flywheel.backend.instancing.InstancedRenderDispatcher;

import me.jellysquid.mods.sodium.client.render.chunk.tasks.ChunkRenderRebuildTask;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderDispatcher;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.world.level.block.entity.BlockEntity;

@Mixin(ChunkRenderRebuildTask.class)
public class ChunkRenderRebuildTaskMixin {
	@Redirect(method = "performBuild", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/blockentity/BlockEntityRenderDispatcher;getRenderer(Lnet/minecraft/world/level/block/entity/BlockEntity;)Lnet/minecraft/client/renderer/blockentity/BlockEntityRenderer;", remap = true), remap = false)
	private BlockEntityRenderer<?> flywheel$redirectGetRenderer(BlockEntityRenderDispatcher dispatcher, BlockEntity blockEntity) {
		if (InstancedRenderDispatcher.tryAddBlockEntity(blockEntity)) {
			return null;
		}
		return dispatcher.getRenderer(blockEntity);
	}
}
