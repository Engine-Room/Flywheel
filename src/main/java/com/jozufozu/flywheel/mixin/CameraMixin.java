package com.jozufozu.flywheel.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.jozufozu.flywheel.core.LastActiveCamera;

import net.minecraft.client.Camera;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.BlockGetter;

@Mixin(Camera.class)
public class CameraMixin {

	@Inject(method = "setup", at = @At("TAIL"))
	private void setup(BlockGetter level, Entity entity, boolean is3rdPerson, boolean isMirrored, float pt, CallbackInfo ci) {
		LastActiveCamera._setActiveCamera((Camera)(Object) this);
	}
}
