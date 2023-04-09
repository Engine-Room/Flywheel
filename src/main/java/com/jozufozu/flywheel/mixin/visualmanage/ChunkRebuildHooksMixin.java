package com.jozufozu.flywheel.mixin.visualmanage;

import java.util.Set;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.jozufozu.flywheel.impl.visualization.VisualizationHelper;
import com.jozufozu.flywheel.impl.visualization.VisualizedRenderDispatcher;
import com.jozufozu.flywheel.util.FlwUtil;

import net.minecraft.client.renderer.chunk.ChunkRenderDispatcher;
import net.minecraft.world.level.block.entity.BlockEntity;

@Mixin(targets = "net.minecraft.client.renderer.chunk.ChunkRenderDispatcher$RenderChunk$RebuildTask")
public class ChunkRebuildHooksMixin {
	@Inject(method = "handleBlockEntity", at = @At("HEAD"), cancellable = true)
	private <E extends BlockEntity> void flywheel$addAndFilterBEs(ChunkRenderDispatcher.CompiledChunk compiledChunk, Set<BlockEntity> set, E be, CallbackInfo ci) {
		if (FlwUtil.canUseVisualization(be.getLevel())) {
			if (VisualizationHelper.canVisualize(be))
				VisualizedRenderDispatcher.getBlockEntities(be.getLevel()).queueAdd(be);

			if (VisualizationHelper.shouldSkipRender(be))
				ci.cancel();
		}
	}
}
