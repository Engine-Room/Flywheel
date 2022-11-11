package com.jozufozu.flywheel.mixin.instancemanage;

import java.util.Collection;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

import net.minecraft.client.renderer.chunk.ChunkRenderDispatcher;
import net.minecraft.world.level.block.entity.BlockEntity;

/**
 * For use in {@link ChunkRebuildHooksMixin#addAndFilterBEs(ChunkRenderDispatcher.RenderChunk, Collection)}
 */
@Mixin(ChunkRenderDispatcher.RenderChunk.class)
public interface RenderChunkAccessor {

	@Invoker("updateGlobalBlockEntities")
	void flywheel$updateGlobalBlockEntities(Collection<BlockEntity> blockEntities);
}
