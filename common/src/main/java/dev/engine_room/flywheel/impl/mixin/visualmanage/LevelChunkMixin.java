package dev.engine_room.flywheel.impl.mixin.visualmanage;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import dev.engine_room.flywheel.api.visualization.VisualizationManager;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.chunk.LevelChunk;

@Mixin(LevelChunk.class)
abstract class LevelChunkMixin {
	@Shadow
	@Final
	Level level;

	@Inject(method = "setBlockEntity(Lnet/minecraft/world/level/block/entity/BlockEntity;)V",
			at = @At(value = "INVOKE_ASSIGN", target = "Ljava/util/Map;put(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;"))
	private void flywheel$onBlockEntityAdded(BlockEntity blockEntity, CallbackInfo ci) {
		VisualizationManager manager = VisualizationManager.get(level);
		if (manager == null) {
			return;
		}

		manager.getBlockEntities().queueAdd(blockEntity);
	}
}
