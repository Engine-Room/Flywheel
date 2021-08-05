package com.jozufozu.flywheel.mixin;

import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.VideoSettingsScreen;

import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.jozufozu.flywheel.backend.OptifineHandler;

import net.minecraft.client.Minecraft;

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
