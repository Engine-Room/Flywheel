package dev.engine_room.flywheel.impl.mixin.fix;

import org.spongepowered.asm.mixin.Mixin;

import net.minecraft.client.renderer.LevelRenderer;

@Mixin(LevelRenderer.class)
abstract class FixFabulousDepthMixin {
	// TODO 1.21.11: Is this still needed?
//	@Inject(method = "renderLevel", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/PostChain;process(F)V", ordinal = 1))
//	private void flywheel$disableTransparencyShaderDepth(CallbackInfo ci) {
//		GlStateManager._depthMask(false);
//	}
}
