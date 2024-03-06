package com.jozufozu.flywheel.backend.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

import net.minecraft.client.Camera;
import net.minecraft.client.renderer.GameRenderer;

@Mixin(GameRenderer.class)
public interface GameRendererAccessor {
	@Invoker("getFov")
	double flywheel$getFov(Camera pActiveRenderInfo, float pPartialTicks, boolean pUseFOVSetting);

	@Accessor("zoom")
	float flywheel$getZoom();

	@Accessor("zoomX")
	float flywheel$getZoomX();

	@Accessor("zoomY")
	float flywheel$getZoomY();
}
