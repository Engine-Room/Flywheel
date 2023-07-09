package com.jozufozu.flywheel.fabric.mixin;

import java.util.List;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.jozufozu.flywheel.event.ForgeEvents;

import net.minecraft.client.gui.components.DebugScreenOverlay;

@Mixin(DebugScreenOverlay.class)
public abstract class DebugScreenOverlayMixin {
	@Inject(method = "getSystemInformation", at = @At("RETURN"))
	private void modifyRightText(CallbackInfoReturnable<List<String>> cir) {
		ForgeEvents.addToDebugScreen(cir.getReturnValue());
	}
}
