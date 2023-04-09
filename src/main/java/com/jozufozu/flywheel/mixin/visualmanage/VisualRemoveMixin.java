package com.jozufozu.flywheel.mixin.visualmanage;

import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.jozufozu.flywheel.impl.visualization.VisualizedRenderDispatcher;
import com.jozufozu.flywheel.util.FlwUtil;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;

@Mixin(BlockEntity.class)
public class VisualRemoveMixin {
	@Shadow
	@Nullable
	protected Level level;

	@Inject(at = @At("TAIL"), method = "setRemoved")
	private void flywheel$removeVisual(CallbackInfo ci) {
		if (level instanceof ClientLevel && FlwUtil.canUseVisualization(level)) {
			VisualizedRenderDispatcher.getBlockEntities(level)
					.remove((BlockEntity) (Object) this);
		}
	}
}
