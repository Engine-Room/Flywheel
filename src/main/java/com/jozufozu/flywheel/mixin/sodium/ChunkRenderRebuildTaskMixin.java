package com.jozufozu.flywheel.mixin.sodium;

import com.jozufozu.flywheel.backend.Backend;
import com.jozufozu.flywheel.backend.instancing.InstancedRenderDispatcher;
import com.jozufozu.flywheel.backend.instancing.InstancedRenderRegistry;

import me.jellysquid.mods.sodium.client.render.chunk.data.ChunkRenderData;
import me.jellysquid.mods.sodium.client.render.chunk.tasks.ChunkRenderRebuildTask;

import net.minecraft.world.level.block.entity.BlockEntity;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(ChunkRenderRebuildTask.class)
public class ChunkRenderRebuildTaskMixin {
	@Redirect(method = "performBuild", at = @At(value = "INVOKE", target = "Lme/jellysquid/mods/sodium/client/render/chunk/data/ChunkRenderData$Builder;addBlockEntity(Lnet/minecraft/world/level/block/entity/BlockEntity;Z)V"))
	private void skipBlockEntities(ChunkRenderData.Builder instance, BlockEntity be, boolean cull) {
		if (Backend.canUseInstancing(be.getLevel())) {
			if (InstancedRenderRegistry.canInstance(be.getType()))
				InstancedRenderDispatcher.getBlockEntities(be.getLevel()).queueAdd(be);

			if (InstancedRenderRegistry.shouldSkipRender(be))
				return;
		}
		instance.addBlockEntity(be, cull);
	}
}
