package dev.engine_room.flywheel.impl.mixin.visualmanage;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Coerce;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import dev.engine_room.flywheel.lib.visualization.VisualizationHelper;
import net.minecraft.world.level.block.entity.BlockEntity;

@Mixin(targets = "net.minecraft.client.renderer.chunk.ChunkRenderDispatcher$RenderChunk$RebuildTask")
abstract class RebuildTaskMixin {
	@Inject(method = "handleBlockEntity(Lnet/minecraft/client/renderer/chunk/ChunkRenderDispatcher$RenderChunk$RebuildTask$CompileResults;Lnet/minecraft/world/level/block/entity/BlockEntity;)V", at = @At("HEAD"), cancellable = true)
	private void flywheel$tryAddBlockEntity(@Coerce Object compileResults, BlockEntity blockEntity, CallbackInfo ci) {
		if (VisualizationHelper.tryAddBlockEntity(blockEntity)) {
			ci.cancel();
		}
	}
}
