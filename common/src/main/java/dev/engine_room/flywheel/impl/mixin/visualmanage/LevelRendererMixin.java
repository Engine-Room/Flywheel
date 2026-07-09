package dev.engine_room.flywheel.impl.mixin.visualmanage;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import dev.engine_room.flywheel.api.visualization.VisualizationManager;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

@Mixin(ClientLevel.class)
abstract class LevelRendererMixin {
	@Unique
	private ClientLevel flywheel$level() {
		return (ClientLevel) (Object) this;
	}

	/**
	 * This gets called when a block is marked for rerender by vanilla.
	 */
	@Inject(method = "setBlocksDirty(Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/world/level/block/state/BlockState;)V", at = @At("TAIL"))
	private void flywheel$checkUpdate(BlockPos pos, BlockState oldState, BlockState newState, CallbackInfo ci) {
		ClientLevel level = flywheel$level();
		VisualizationManager manager = VisualizationManager.get(level);
		if (manager == null) {
			return;
		}

		BlockEntity blockEntity = level.getBlockEntity(pos);
		if (blockEntity == null) {
			return;
		}

		var blockEntities = manager.blockEntities();
		if (oldState != newState) {
			blockEntities.queueRemove(blockEntity);
			blockEntities.queueAdd(blockEntity);
		} else {
			// I don't think this is possible to reach in vanilla
			blockEntities.queueUpdate(blockEntity);
		}
	}
}
