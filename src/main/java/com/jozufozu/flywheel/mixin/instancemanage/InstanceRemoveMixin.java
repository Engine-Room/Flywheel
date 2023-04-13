package com.jozufozu.flywheel.mixin.instancemanage;

import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.jozufozu.flywheel.impl.instancing.InstancedRenderDispatcher;
import com.jozufozu.flywheel.util.FlwUtil;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;

@Mixin(BlockEntity.class)
public class InstanceRemoveMixin {
	@Shadow
	@Nullable
	protected Level level;

	@Inject(at = @At("TAIL"), method = "setRemoved")
	private void flywheel$removeInstance(CallbackInfo ci) {
		if (level instanceof ClientLevel && FlwUtil.canUseInstancing(level)) {
			InstancedRenderDispatcher.getBlockEntities(level)
					.queueRemove((BlockEntity) (Object) this);
		}
	}

//	/**
//	 * Don't do this.
//	 * It can cause infinite loops if an instance class tries to access another block entity in its constructor.
//	 */
//	@Inject(at = @At("TAIL"), method = "clearRemoved")
//	private void addInstance(CallbackInfo ci) {
//		if (level.isClientSide) InstancedRenderDispatcher.getBlockEntities(this.level)
//				.add((BlockEntity) (Object) this);
//	}
}
