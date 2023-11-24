package com.jozufozu.flywheel.mixin.visualmanage;

import java.util.Set;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.jozufozu.flywheel.impl.visualization.VisualizationHelper;

import net.minecraft.client.renderer.chunk.ChunkRenderDispatcher;
import net.minecraft.client.renderer.chunk.RenderChunkRegion;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;

@Mixin(targets = "net.minecraft.client.renderer.chunk.ChunkRenderDispatcher$RenderChunk$RebuildTask")
public class ChunkRebuildHooksMixin {

// FIXME: use this instead of the redirect if there's a clean way to reference the CompileResults
//	@Inject(method = "handleBlockEntity(Lnet/minecraft/client/renderer/chunk/ChunkRenderDispatcher$RenderChunk$RebuildTask$CompileResults;Lnet/minecraft/world/level/block/entity/BlockEntity;)V", at = @At("HEAD"), cancellable = true)
//	private <E extends BlockEntity> void flywheel$tryAddBlockEntity(Object pCompileResults, E pBlockEntity, CallbackInfo ci) {
//		if (VisualizationHelper.tryAddBlockEntity(pBlockEntity)) {
//			ci.cancel();
//		}
//	}

	@Redirect(method = "compile", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/chunk/RenderChunkRegion;getBlockEntity(Lnet/minecraft/core/BlockPos;)Lnet/minecraft/world/level/block/entity/BlockEntity;"))
	private BlockEntity flywheel$interceptGetBlockEntity(RenderChunkRegion region, BlockPos pos) {
		BlockEntity blockEntity = region.getBlockEntity(pos);

		if (blockEntity == null) {
			return null;
		}

		if (VisualizationHelper.tryAddBlockEntity(blockEntity)) {
			return null;
		}

		return blockEntity;
	}
}
