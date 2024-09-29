package dev.engine_room.flywheel.backend.mixin;

import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.mojang.blaze3d.platform.GlStateManager;

import dev.engine_room.flywheel.backend.engine.uniform.LevelUniforms;
import dev.engine_room.flywheel.backend.gl.GlStateTracker;
import dev.engine_room.flywheel.backend.gl.buffer.GlBufferType;

@Mixin(value = GlStateManager.class, remap = false)
abstract class GlStateManagerMixin {
	@Inject(method = "_glBindBuffer(II)V", at = @At("RETURN"))
	private static void flywheel$onBindBuffer(int target, int buffer, CallbackInfo ci) {
		GlStateTracker._setBuffer(GlBufferType.fromTarget(target), buffer);
	}

	@Inject(method = "_glBindVertexArray(I)V", at = @At("RETURN"))
	private static void flywheel$onBindVertexArray(int array, CallbackInfo ci) {
		GlStateTracker._setVertexArray(array);
	}

	@Inject(method = "_glUseProgram(I)V", at = @At("RETURN"))
	private static void flywheel$onUseProgram(int program, CallbackInfo ci) {
		GlStateTracker._setProgram(program);
	}

	@Inject(method = "setupLevelDiffuseLighting", at = @At("HEAD"))
	private static void flywheel$onSetupLevelDiffuseLighting(Vector3f vector3f, Vector3f vector3f2, Matrix4f matrix4f, CallbackInfo ci) {
		// Capture the light directions before they're transformed into screen space
		// Basically all usages of assigning light direction go through here so I think this is safe
		LevelUniforms.LIGHT0_DIRECTION.set(vector3f);
		LevelUniforms.LIGHT1_DIRECTION.set(vector3f2);
	}
}
