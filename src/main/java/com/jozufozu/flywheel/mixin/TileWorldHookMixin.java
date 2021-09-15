package com.jozufozu.flywheel.mixin;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.jozufozu.flywheel.backend.instancing.InstancedRenderDispatcher;

import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
@Mixin(value = LevelChunk.class, priority = 1100) // this and create.mixins.json have high priority to load after Performant
public class TileWorldHookMixin {

	@Shadow
	@Final
	Level level;

	@Inject(at = @At(value = "INVOKE_ASSIGN", target = "Lnet/minecraft/world/level/block/entity/BlockEntity;clearRemoved()V"),
			method = "setBlockEntity")
	private void onAddTile(BlockEntity te, CallbackInfo ci) {
		if (level.isClientSide) {
			InstancedRenderDispatcher.getTiles(level)
					.queueAdd(te);
		}
	}
}
