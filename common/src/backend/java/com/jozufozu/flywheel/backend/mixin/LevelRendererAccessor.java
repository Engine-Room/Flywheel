package com.jozufozu.flywheel.backend.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import net.minecraft.client.renderer.LevelRenderer;

@Mixin(LevelRenderer.class)
public interface LevelRendererAccessor {
	@Accessor("ticks")
	int flywheel$getTicks();
}
