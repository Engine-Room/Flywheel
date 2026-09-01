package dev.engine_room.flywheel.backend.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import com.mojang.blaze3d.opengl.GlStateManager;

@Mixin(GlStateManager.class)
public interface GlStateManagerAccessor {
	@Accessor("activeTexture")
	static int flywheel$getActiveTexture() {
		throw new AssertionError();
	}
}
