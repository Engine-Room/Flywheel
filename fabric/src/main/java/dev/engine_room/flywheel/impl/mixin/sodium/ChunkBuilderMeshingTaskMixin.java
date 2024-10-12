package dev.engine_room.flywheel.impl.mixin.sodium;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;

import dev.engine_room.flywheel.lib.visualization.VisualizationHelper;
import me.jellysquid.mods.sodium.client.render.chunk.compile.tasks.ChunkBuilderMeshingTask;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderDispatcher;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.world.level.block.entity.BlockEntity;

@Mixin(value = ChunkBuilderMeshingTask.class, remap = false)
abstract class ChunkBuilderMeshingTaskMixin {
	@WrapOperation(
			method = "execute(Lme/jellysquid/mods/sodium/client/render/chunk/compile/ChunkBuildContext;Lme/jellysquid/mods/sodium/client/util/task/CancellationToken;)Lme/jellysquid/mods/sodium/client/render/chunk/compile/ChunkBuildOutput;",
			at = @At(
					value = "INVOKE",
					target = "Lnet/minecraft/client/renderer/blockentity/BlockEntityRenderDispatcher;getRenderer(Lnet/minecraft/world/level/block/entity/BlockEntity;)Lnet/minecraft/client/renderer/blockentity/BlockEntityRenderer;",
					remap = true
			)
	)
	private BlockEntityRenderer<?> flywheel$wrapGetRenderer(BlockEntityRenderDispatcher instance, BlockEntity blockEntity, Operation<BlockEntityRenderer<BlockEntity>> original) {
		if (VisualizationHelper.tryAddBlockEntity(blockEntity)) {
			return null;
		}
		return original.call(instance, blockEntity);
	}
}
