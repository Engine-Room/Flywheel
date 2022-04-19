package com.jozufozu.flywheel.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.jozufozu.flywheel.backend.Backend;
import com.jozufozu.flywheel.backend.gl.GlStateTracker;
import com.jozufozu.flywheel.core.RenderContext;
import com.jozufozu.flywheel.event.RenderLayerEvent;

import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderBuffers;
import net.minecraft.client.renderer.RenderType;
import net.minecraftforge.common.MinecraftForge;

@Mixin(MultiBufferSource.BufferSource.class)
public class MultiBufferSourceMixin {

	@Inject(method = "endBatch(Lnet/minecraft/client/renderer/RenderType;)V", at = @At("TAIL"))
	private void renderLayer(RenderType renderType, CallbackInfo ci) {
		if (RenderContext.CURRENT != null && Backend.isGameActive() && Backend.isOn()) {
			GlStateTracker.State restoreState = GlStateTracker.getRestoreState();

			MinecraftForge.EVENT_BUS.post(new RenderLayerEvent(RenderContext.CURRENT.withRenderType(renderType)));

			restoreState.restore();
		}
	}
}
