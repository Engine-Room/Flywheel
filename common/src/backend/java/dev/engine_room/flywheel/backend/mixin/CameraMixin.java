package dev.engine_room.flywheel.backend.mixin;

import org.joml.Vector3f;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import dev.engine_room.flywheel.backend.CameraRenderStateExtension;
import net.minecraft.client.Camera;
import net.minecraft.client.renderer.state.level.CameraRenderState;

@Mixin(Camera.class)
abstract class CameraMixin {
	@Shadow
	@Final
	private Vector3f forwards;

	@Inject(method = "extractRenderState", at = @At("TAIL"))
	private void flywheel$extendRenderState(CameraRenderState cameraState, float cameraEntityPartialTicks, CallbackInfo ci) {
		((CameraRenderStateExtension) cameraState).flywheel$getForwardVector().set(this.forwards);
	}
}
