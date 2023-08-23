package com.jozufozu.flywheel.mixin.instancemanage;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.jozufozu.flywheel.backend.Backend;
import com.jozufozu.flywheel.backend.instancing.InstancedRenderDispatcher;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

@Mixin(LevelRenderer.class)
public class InstanceUpdateMixin {
	@Shadow
	private ClientLevel level;

	/**
	 * This gets called when a block is marked for rerender by vanilla.
	 */
	@Inject(method = "setBlockDirty(Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/world/level/block/state/BlockState;)V", at = @At("TAIL"))
	private void flywheel$checkUpdate(BlockPos pos, BlockState oldState, BlockState newState, CallbackInfo ci) {
		if (!Backend.isOn()) {
			return;
		}

		BlockEntity blockEntity = level.getBlockEntity(pos);
		if (blockEntity == null) {
			return;
		}

		InstancedRenderDispatcher.getBlockEntities(level)
				.queueUpdate(blockEntity);
	}
}
