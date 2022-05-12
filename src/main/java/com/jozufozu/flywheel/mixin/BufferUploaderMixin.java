package com.jozufozu.flywheel.mixin;

import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.jozufozu.flywheel.backend.gl.GlStateTracker;
import com.mojang.blaze3d.vertex.BufferUploader;
import com.mojang.blaze3d.vertex.VertexFormat;

@Mixin(BufferUploader.class)
public class BufferUploaderMixin {

	@Shadow
	@Nullable
	private static VertexFormat lastFormat;

	@Inject(method = "reset", at = @At("HEAD"))
	private static void stopBufferUploaderFromClearingBufferStateIfNothingIsBound(CallbackInfo ci) {
		// Trust our tracker over BufferUploader's.
		if (GlStateTracker.getVertexArray() == 0) {
			lastFormat = null;
		}
	}
}
