package com.jozufozu.flywheel.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Group;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import com.jozufozu.flywheel.backend.OptifineHandler;
import com.jozufozu.flywheel.event.BeginFrameEvent;
import com.mojang.blaze3d.matrix.MatrixStack;

import net.minecraft.client.renderer.ActiveRenderInfo;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.renderer.culling.ClippingHelper;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.profiler.IProfiler;
import net.minecraft.util.math.vector.Matrix4f;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.MinecraftForge;

@OnlyIn(Dist.CLIENT)
@Mixin(WorldRenderer.class)
public class BeginFrameMixin {

	@Shadow
	private ClientWorld level;

	@Unique
	private boolean setup;

	/**
	 * This version gets run by default.
	 */
	@Group(name = "setupRender", min = 1)
	@Inject(method = "renderLevel",
			at = @At(value = "INVOKE", target = "net.minecraft.client.renderer.WorldRenderer.compileChunksUntil(J)V"),
			locals = LocalCapture.CAPTURE_FAILSOFT)
	private void setupRender(MatrixStack stack, float p_228426_2_, long p_228426_3_, boolean p_228426_5_,
			ActiveRenderInfo info, GameRenderer gameRenderer, LightTexture lightTexture, Matrix4f projection,
			CallbackInfo ci, // locals, only care about clippinghelper
			IProfiler iprofiler, Vector3d vector3d, double d0, double d1, double d2, Matrix4f matrix4f, boolean flag,
			ClippingHelper clippinghelper) {

		if (!OptifineHandler.usingShaders()) {
			MinecraftForge.EVENT_BUS.post(new BeginFrameEvent(level, info, clippinghelper));

			setup = true;
		}
	}

	/**
	 * This version gets run when optifine is installed and shaders are enabled.
	 */
	@Group(name = "setupRender")
	@Inject(at = @At("HEAD"), method = "setupRender")
	private void setupRender2(ActiveRenderInfo info, ClippingHelper clippingHelper, boolean p_228437_3_, int frameCount, boolean isSpectator, CallbackInfo ci) {
		if (!setup) {
			MinecraftForge.EVENT_BUS.post(new BeginFrameEvent(level, info, clippingHelper));
		}
		setup = false;
	}
}
