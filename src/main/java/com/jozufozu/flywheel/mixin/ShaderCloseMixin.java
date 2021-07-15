package com.jozufozu.flywheel.mixin;

import javax.annotation.Nullable;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.jozufozu.flywheel.backend.OptifineHandler;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.VideoSettingsScreen;

@Mixin(Minecraft.class)
public class ShaderCloseMixin {

	@Shadow
	@Nullable
	public Screen screen;

	@Inject(at = @At("HEAD"), method = "setScreen")
	private void whenScreenChanges(Screen screen, CallbackInfo info) {
		if (OptifineHandler.optifineInstalled() && screen instanceof VideoSettingsScreen) {
			Screen old = this.screen;
			if (old != null && old.getClass()
					.getName()
					.startsWith(OptifineHandler.SHADER_PACKAGE)) {
				OptifineHandler.refresh();
			}
		}
	}
}
