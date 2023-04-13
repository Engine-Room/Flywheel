package com.jozufozu.flywheel.mixin.visualmanage;

import java.util.Set;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.jozufozu.flywheel.impl.visualization.VisualizedRenderDispatcher;

import net.minecraft.client.renderer.chunk.ChunkRenderDispatcher;
import net.minecraft.world.level.block.entity.BlockEntity;

@Mixin(targets = "net.minecraft.client.renderer.chunk.ChunkRenderDispatcher$RenderChunk$RebuildTask")
public class ChunkRebuildHooksMixin {
	@Inject(method = "handleBlockEntity(Lnet/minecraft/client/renderer/chunk/ChunkRenderDispatcher$CompiledChunk;Ljava/util/Set;Lnet/minecraft/world/level/block/entity/BlockEntity;)V", at = @At("HEAD"), cancellable = true)
	private void flywheel$tryAddBlockEntity(ChunkRenderDispatcher.CompiledChunk compiledChunk, Set<BlockEntity> globalBlockEntities, BlockEntity blockEntity, CallbackInfo ci) {
		if (VisualizedRenderDispatcher.tryAddBlockEntity(blockEntity)) {
			ci.cancel();
		}
	}
}
