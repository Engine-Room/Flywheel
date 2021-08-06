package com.jozufozu.flywheel.mixin;

import com.jozufozu.flywheel.backend.instancing.InstancedRenderDispatcher;

import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.chunk.LevelChunk;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LevelChunk.class)
public class LevelChunkMixin {
	@Shadow
	@Final
	private Level level;

	@Inject(at = @At("TAIL"), method = "setBlockEntity")
	private void onAddTile(BlockEntity blockEntity, CallbackInfo ci) {
		if (level.isClientSide) {
			InstancedRenderDispatcher.getTiles(this.level)
					.queueAdd(blockEntity);
		}
	}
}
