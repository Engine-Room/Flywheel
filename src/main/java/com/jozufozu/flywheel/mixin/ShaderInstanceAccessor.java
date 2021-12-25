package com.jozufozu.flywheel.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import net.minecraft.client.renderer.ShaderInstance;

@Mixin(ShaderInstance.class)
public interface ShaderInstanceAccessor {
	@Accessor("lastProgramId")
	static void flywheel$setLastProgramId(int id) {
		throw new AssertionError();
	}
}
