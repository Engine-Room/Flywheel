package dev.engine_room.flywheel.impl.mixin.fabric;

import java.util.List;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import dev.engine_room.flywheel.impl.FlwDebugInfo;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.DebugScreenOverlay;

@Mixin(DebugScreenOverlay.class)
abstract class DebugScreenOverlayMixin {
	@Shadow
	@Final
	private Minecraft minecraft;

	@Inject(method = "getSystemInformation", at = @At("RETURN"))
	private void flywheel$onGetSystemInformation(CallbackInfoReturnable<List<String>> cir) {
		FlwDebugInfo.addDebugInfo(minecraft, cir.getReturnValue());
	}
}
