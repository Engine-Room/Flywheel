package com.jozufozu.flywheel.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.client.main.Main;

@Mixin(value = Main.class, remap = false)
public class ClientMainMixin {

	@Inject(method = "main", at = @At("HEAD"))
	private static void injectRenderDoc(CallbackInfo ci) {
		// Only try to load RenderDoc if a system property is set.
		if (System.getProperty("flw.loadRenderDoc") == null) {
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
