package com.jozufozu.flywheel.mixin;

import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.jozufozu.flywheel.backend.instancing.batching.DrawBuffer;
import com.jozufozu.flywheel.util.RenderTypeExtension;

import net.minecraft.client.renderer.RenderType;

@Mixin(RenderType.class)
public class RenderTypeMixin implements RenderTypeExtension {
	@Unique
	private DrawBuffer flywheel$drawBuffer;

	@Inject(method = "<init>", at = @At("RETURN"))
	private void onReturnInit(CallbackInfo ci) {
		flywheel$drawBuffer = new DrawBuffer((RenderType) (Object) this);
	}

	@Override
	@NotNull
	public DrawBuffer flywheel$getDrawBuffer() {
		return flywheel$drawBuffer;
	}
}
