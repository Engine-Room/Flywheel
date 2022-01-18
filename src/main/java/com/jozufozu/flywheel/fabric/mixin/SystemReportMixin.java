package com.jozufozu.flywheel.fabric.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.jozufozu.flywheel.backend.Backend;

import net.minecraft.SystemReport;

@Mixin(value = SystemReport.class, priority = 1100)
public class SystemReportMixin {
	@Inject(method = "<init>()V", at = @At("TAIL"))
	private void onTailInit(CallbackInfo ci) {
		SystemReport self = (SystemReport) (Object) this;
		self.setDetail("Flywheel Backend", Backend::getBackendDescriptor);
	}
}
