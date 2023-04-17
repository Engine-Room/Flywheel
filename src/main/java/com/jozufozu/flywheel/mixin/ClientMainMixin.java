package com.jozufozu.flywheel.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.client.main.Main;

@Mixin(Main.class)
public class ClientMainMixin {
	@Inject(method = "main([Ljava/lang/String;)V", at = @At("HEAD"))
	private static void flywheel$injectRenderDoc(CallbackInfo ci) {
		// Only try to load RenderDoc if a system property is set to true.
		if (!Boolean.getBoolean("flw.loadRenderDoc")) {
			return;
		}

		try {
			System.loadLibrary("renderdoc");
		} catch (Throwable ignored) {
			// Oh well, we tried.
			// On Windows, RenderDoc installs to "C:\Program Files\RenderDoc\"
			System.err.println("Is RenderDoc in your PATH?");
		}
	}
}
