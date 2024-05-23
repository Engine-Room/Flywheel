package dev.engine_room.flywheel.impl.mixin.fabric;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import dev.engine_room.flywheel.impl.BackendManagerImpl;
import net.minecraft.SystemReport;

@Mixin(value = SystemReport.class, priority = 1056)
abstract class SystemReportMixin {
	@Inject(method = "<init>", at = @At("RETURN"))
	private void flywheel$onInit(CallbackInfo ci) {
		SystemReport self = (SystemReport) (Object) this;
		self.setDetail("Flywheel Backend", BackendManagerImpl::getBackendString);
	}
}
