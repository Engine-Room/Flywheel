package com.jozufozu.flywheel.mixin.visualmanage;

import java.util.Set;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.jozufozu.flywheel.impl.visualization.VisualizationHelper;

import net.minecraft.world.level.block.entity.BlockEntity;

@Mixin(targets = "net.minecraft.client.renderer.chunk.SectionRenderDispatcher$RenderSection$RebuildTask")
public class ChunkRebuildHooksMixin {
	@Inject(method = "handleBlockEntity(Lnet/minecraft/client/renderer/chunk/SectionRenderDispatcher$RenderSection$RebuildTask$CompileResults;Lnet/minecraft/world/level/block/entity/BlockEntity;)V", at = @At("HEAD"), cancellable = true)
	private <E extends BlockEntity> void flywheel$tryAddBlockEntity(Object pCompileResults, E pBlockEntity, CallbackInfo ci) {
		if (VisualizationHelper.tryAddBlockEntity(pBlockEntity)) {
			ci.cancel();
		}
	}
}
