package com.jozufozu.flywheel.mixin.fabric;

import java.util.List;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.jozufozu.flywheel.event.ForgeEvents;

import net.minecraft.client.gui.AbstractGui;
import net.minecraft.client.gui.overlay.DebugOverlayGui;

@Mixin(DebugOverlayGui.class)
public abstract class DebugOverlayGuiMixin extends AbstractGui {
	@Inject(method = "getDebugInfoRight", at = @At("RETURN"))
    private void modifyRightText(CallbackInfoReturnable<List<String>> cir) {
		ForgeEvents.addToDebugScreen(cir.getReturnValue());
	}
}
