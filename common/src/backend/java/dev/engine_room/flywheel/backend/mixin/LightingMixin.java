package dev.engine_room.flywheel.backend.mixin;

import org.joml.Vector3f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.mojang.blaze3d.platform.Lighting;
import com.mojang.blaze3d.platform.Lighting.Entry;

import dev.engine_room.flywheel.backend.engine.uniform.LevelUniforms;

@Mixin(Lighting.class)
abstract class LightingMixin {
	@Inject(method = "updateBuffer(Lcom/mojang/blaze3d/platform/Lighting$Entry;Lorg/joml/Vector3f;Lorg/joml/Vector3f;)V", at = @At("HEAD"))
	private static void flywheel$onHeadUpdateBuffer(Entry entry, Vector3f light0, Vector3f light1, CallbackInfo ci) {
		if (entry == Entry.LEVEL) {
			LevelUniforms.LIGHT0_DIRECTION.set(light0);
			LevelUniforms.LIGHT1_DIRECTION.set(light1);
		}
	}
}
