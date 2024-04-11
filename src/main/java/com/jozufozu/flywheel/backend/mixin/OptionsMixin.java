package com.jozufozu.flywheel.backend.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.jozufozu.flywheel.backend.engine.uniform.OptionsUniforms;

import net.minecraft.client.Options;

@Mixin(Options.class)
public class OptionsMixin {
	@Inject(method = "load()V", at = @At("RETURN"))
	private void flywheel$onLoad(CallbackInfo ci) {
		OptionsUniforms.update((Options) (Object) this);
	}

	@Inject(method = "save", at = @At("HEAD"))
	private void flywheel$onSave(CallbackInfo ci) {
		OptionsUniforms.update((Options) (Object) this);
	}
}
