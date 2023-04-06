package com.jozufozu.flywheel.mixin.instancemanage;

import java.util.Set;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.jozufozu.flywheel.backend.BackendUtil;
import com.jozufozu.flywheel.impl.instancing.InstancedRenderDispatcher;
import com.jozufozu.flywheel.impl.instancing.InstancingControllerHelper;

import net.minecraft.client.renderer.chunk.ChunkRenderDispatcher;
import net.minecraft.world.level.block.entity.BlockEntity;

@Mixin(targets = "net.minecraft.client.renderer.chunk.ChunkRenderDispatcher$RenderChunk$RebuildTask")
public class ChunkRebuildHooksMixin {
	@Inject(method = "handleBlockEntity", at = @At("HEAD"), cancellable = true)
	private <E extends BlockEntity> void flywheel$addAndFilterBEs(ChunkRenderDispatcher.CompiledChunk compiledChunk, Set<BlockEntity> set, E be, CallbackInfo ci) {
		if (BackendUtil.canUseInstancing(be.getLevel())) {
			if (InstancingControllerHelper.canInstance(be))
				InstancedRenderDispatcher.getBlockEntities(be.getLevel()).queueAdd(be);

			if (InstancingControllerHelper.shouldSkipRender(be))
				ci.cancel();
		}
	}
}
