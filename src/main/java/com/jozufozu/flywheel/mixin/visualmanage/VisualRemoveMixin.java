package com.jozufozu.flywheel.mixin.visualmanage;

import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.jozufozu.flywheel.impl.visualization.VisualizedRenderDispatcher;
import com.jozufozu.flywheel.util.FlwUtil;

import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;

@Mixin(BlockEntity.class)
public class VisualRemoveMixin {
	@Shadow
	@Nullable
	protected Level level;

	@Inject(method = "setRemoved()V", at = @At("TAIL"))
	private void flywheel$removeVisual(CallbackInfo ci) {
		if (!FlwUtil.canUseVisualization(level)) {
			return;
		}

		VisualizedRenderDispatcher.getBlockEntities(level)
				.remove((BlockEntity) (Object) this);
	}
}
