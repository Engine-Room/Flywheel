package com.jozufozu.flywheel.mixin.visualmanage;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.jozufozu.flywheel.impl.visualization.VisualizedRenderDispatcher;
import com.jozufozu.flywheel.util.FlwUtil;

import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.chunk.LevelChunk;

@Mixin(LevelChunk.class)
public class VisualAddMixin {
	@Shadow
	@Final
	Level level;

	@Inject(method = "setBlockEntity(Lnet/minecraft/world/level/block/entity/BlockEntity;)V",
			at = @At(value = "INVOKE_ASSIGN", target = "Ljava/util/Map;put(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;"))
	private void flywheel$onBlockEntityAdded(BlockEntity blockEntity, CallbackInfo ci) {
		if (!FlwUtil.canUseVisualization(level)) {
			return;
		}

		VisualizedRenderDispatcher.getBlockEntities(level)
				.queueAdd(blockEntity);
	}
}
