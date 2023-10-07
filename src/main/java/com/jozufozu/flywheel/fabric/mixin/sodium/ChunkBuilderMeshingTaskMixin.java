package com.jozufozu.flywheel.fabric.mixin.sodium;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import com.jozufozu.flywheel.backend.Backend;
import com.jozufozu.flywheel.backend.instancing.InstancedRenderDispatcher;
import com.jozufozu.flywheel.backend.instancing.InstancedRenderRegistry;

import me.jellysquid.mods.sodium.client.render.chunk.compile.tasks.ChunkBuilderMeshingTask;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderDispatcher;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.world.level.block.entity.BlockEntity;

@Mixin(value = ChunkBuilderMeshingTask.class, remap = false)
public class ChunkBuilderMeshingTaskMixin {
	@Redirect(method = "execute", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/blockentity/BlockEntityRenderDispatcher;getRenderer(Lnet/minecraft/world/level/block/entity/BlockEntity;)Lnet/minecraft/client/renderer/blockentity/BlockEntityRenderer;", remap = true))
	private BlockEntityRenderer<?> redirectGetRenderer(BlockEntityRenderDispatcher dispatcher, BlockEntity blockEntity) {
		if (Backend.canUseInstancing(blockEntity.getLevel())) {
			if (InstancedRenderRegistry.canInstance(blockEntity.getType()))
				InstancedRenderDispatcher.getBlockEntities(blockEntity.getLevel()).queueAdd(blockEntity);

			if (InstancedRenderRegistry.shouldSkipRender(blockEntity))
				return null;
		}
		return dispatcher.getRenderer(blockEntity);
	}
}
