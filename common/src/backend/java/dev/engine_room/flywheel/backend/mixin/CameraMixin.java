package dev.engine_room.flywheel.backend.mixin;

import org.joml.Vector3fc;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import dev.engine_room.flywheel.backend.extension.CameraRenderStateExtension;
import net.minecraft.client.Camera;
import net.minecraft.client.renderer.state.level.CameraRenderState;

@Mixin(Camera.class)
public abstract class CameraMixin {
	@Shadow
	public abstract Vector3fc forwardVector();

	/// Adds the camera's forward vector to {@link CameraRenderState}
	///
	/// Also see {@link CameraRenderStateMixin} and {@link CameraRenderStateExtension} which add the field to {@link CameraRenderState}
	@Inject(method = "extractRenderState", at = @At("TAIL"))
	private void flywheel$extendRenderState(CameraRenderState cameraState, float cameraEntityPartialTicks, CallbackInfo ci) {
		((CameraRenderStateExtension) cameraState).flywheel$getForwardVector().set(forwardVector());
	}
}
