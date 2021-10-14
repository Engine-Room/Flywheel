package com.jozufozu.flywheel.mixin;

import java.util.Set;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.jozufozu.flywheel.backend.Backend;
import com.jozufozu.flywheel.backend.instancing.InstancedRenderRegistry;

import net.minecraft.client.renderer.chunk.ChunkRenderDispatcher;
import net.minecraft.world.level.block.entity.BlockEntity;

@Mixin(targets = "net.minecraft.client.renderer.chunk.ChunkRenderDispatcher$RenderChunk$RebuildTask")
public class ChunkRebuildHooksMixin {

	@Inject(method = "handleBlockEntity", at = @At("HEAD"), cancellable = true)
	private <E extends BlockEntity> void filterBlockEntity(ChunkRenderDispatcher.CompiledChunk compiledChunk, Set<BlockEntity> set, E be, CallbackInfo ci) {

		if (Backend.getInstance().canUseInstancing() && Backend.isFlywheelWorld(be.getLevel()) && InstancedRenderRegistry.getInstance()
				.shouldSkipRender(be)) {
			ci.cancel();
		}
	}
}
