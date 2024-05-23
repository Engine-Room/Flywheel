package com.jozufozu.flywheel.impl.mixin.fabric;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.jozufozu.flywheel.lib.util.LevelAttached;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;

@Mixin(Minecraft.class)
abstract class MinecraftMixin {
	@Shadow
	public ClientLevel level;

	@Inject(method = "setLevel(Lnet/minecraft/client/multiplayer/ClientLevel;)V", at = @At("HEAD"))
	private void flywheel$onSetLevel(CallbackInfo ci) {
		if (level != null) {
			LevelAttached.invalidateLevel(level);
		}
	}

	@Inject(method = "clearLevel(Lnet/minecraft/client/gui/screens/Screen;)V", at = @At("HEAD"))
	private void flywheel$onClearLevel(CallbackInfo ci) {
		if (level != null) {
			LevelAttached.invalidateLevel(level);
		}
	}
}
