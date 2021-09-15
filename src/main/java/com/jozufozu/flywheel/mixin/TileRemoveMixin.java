package com.jozufozu.flywheel.mixin;

import javax.annotation.Nullable;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.jozufozu.flywheel.backend.instancing.InstancedRenderDispatcher;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.Level;

@Mixin(BlockEntity.class)
public class TileRemoveMixin {

	@Shadow
	@Nullable
	protected Level level;

	@Inject(at = @At("TAIL"), method = "setRemoved")
	private void onRemove(CallbackInfo ci) {
		if (level instanceof ClientLevel) InstancedRenderDispatcher.getTiles(this.level)
				.remove((BlockEntity) (Object) this);
	}
}
