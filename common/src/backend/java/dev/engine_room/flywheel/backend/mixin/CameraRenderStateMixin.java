package dev.engine_room.flywheel.backend.mixin;

import org.joml.Vector3f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

import dev.engine_room.flywheel.backend.CameraRenderStateExtension;
import net.minecraft.client.renderer.state.level.CameraRenderState;

@Mixin(CameraRenderState.class)
abstract class CameraRenderStateMixin implements CameraRenderStateExtension {
	@Unique private final Vector3f flywheel$forwardVector = new Vector3f();

	@Override
	public Vector3f flywheel$getForwardVector() {
		return this.flywheel$forwardVector;
	}
}
