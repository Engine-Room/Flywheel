package com.jozufozu.flywheel.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.jozufozu.flywheel.backend.Backend;
import com.jozufozu.flywheel.backend.gl.GlStateTracker;
import com.jozufozu.flywheel.backend.instancing.InstancedRenderDispatcher;
import com.jozufozu.flywheel.core.RenderContext;
import com.jozufozu.flywheel.event.RenderLayerEvent;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Matrix4f;

import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraftforge.common.MinecraftForge;

@Mixin(value = LevelRenderer.class, priority = 1001) // Higher priority to go after sodium
public class LevelRendererDispatchMixin {

	@Inject(at = @At("TAIL"), method = "renderChunkLayer")
	private void renderChunkLayer(RenderType pRenderType, PoseStack pPoseStack, double pCamX, double pCamY, double pCamZ, Matrix4f pProjectionMatrix, CallbackInfo ci) {
		try (var restoreState = GlStateTracker.getRestoreState()) {

			// TODO: Is this necessary?
			InstancedRenderDispatcher.renderSpecificType(RenderContext.CURRENT, pRenderType);
			MinecraftForge.EVENT_BUS.post(new RenderLayerEvent(RenderContext.CURRENT, pRenderType));
		}
	}

	@Inject(method = "renderLevel", at = @At(value = "INVOKE", ordinal = 0, target = "Lnet/minecraft/client/renderer/MultiBufferSource$BufferSource;endBatch()V"))
	private void endBatch(CallbackInfo ci) {
		if (RenderContext.CURRENT != null && Backend.isGameActive() && Backend.isOn()) {
			try (var restoreState = GlStateTracker.getRestoreState()) {
				InstancedRenderDispatcher.renderAllRemaining(RenderContext.CURRENT);
			}
		}
	}

	@Unique
	private void flywheel$dispatch(RenderType pRenderType) {
		if (RenderContext.CURRENT != null && Backend.isGameActive() && Backend.isOn()) {
			try (var restoreState = GlStateTracker.getRestoreState()) {
				InstancedRenderDispatcher.renderSpecificType(RenderContext.CURRENT, pRenderType);
			}
		}
	}

	@Inject(method = "renderLevel", at = @At(value = "INVOKE", shift = At.Shift.AFTER, ordinal = 0, target = "Lnet/minecraft/client/renderer/MultiBufferSource$BufferSource;endBatch(Lnet/minecraft/client/renderer/RenderType;)V"))
	private void renderLayer$entitySolid(CallbackInfo ci) {
		flywheel$dispatch(RenderType.entitySolid(TextureAtlas.LOCATION_BLOCKS));
	}

	@Inject(method = "renderLevel", at = @At(value = "INVOKE", shift = At.Shift.AFTER, ordinal = 1, target = "Lnet/minecraft/client/renderer/MultiBufferSource$BufferSource;endBatch(Lnet/minecraft/client/renderer/RenderType;)V"))
	private void renderLayer$entityCutout(CallbackInfo ci) {
		flywheel$dispatch(RenderType.entityCutout(TextureAtlas.LOCATION_BLOCKS));
	}

	@Inject(method = "renderLevel", at = @At(value = "INVOKE", shift = At.Shift.AFTER, ordinal = 2, target = "Lnet/minecraft/client/renderer/MultiBufferSource$BufferSource;endBatch(Lnet/minecraft/client/renderer/RenderType;)V"))
	private void renderLayer$entityCutoutNoCull(CallbackInfo ci) {
		flywheel$dispatch(RenderType.entityCutoutNoCull(TextureAtlas.LOCATION_BLOCKS));
	}

	@Inject(method = "renderLevel", at = @At(value = "INVOKE", shift = At.Shift.AFTER, ordinal = 3, target = "Lnet/minecraft/client/renderer/MultiBufferSource$BufferSource;endBatch(Lnet/minecraft/client/renderer/RenderType;)V"))
	private void renderLayer$entitySmoothCutout(CallbackInfo ci) {
		flywheel$dispatch(RenderType.entitySmoothCutout(TextureAtlas.LOCATION_BLOCKS));
	}

	@Inject(method = "renderLevel", at = @At(value = "INVOKE", shift = At.Shift.AFTER, ordinal = 4, target = "Lnet/minecraft/client/renderer/MultiBufferSource$BufferSource;endBatch(Lnet/minecraft/client/renderer/RenderType;)V"))
	private void renderLayer$solid(CallbackInfo ci) {
		flywheel$dispatch(RenderType.solid());
	}

	@Inject(method = "renderLevel", at = @At(value = "INVOKE", shift = At.Shift.AFTER, ordinal = 5, target = "Lnet/minecraft/client/renderer/MultiBufferSource$BufferSource;endBatch(Lnet/minecraft/client/renderer/RenderType;)V"))
	private void renderLayer$endPortal(CallbackInfo ci) {
		flywheel$dispatch(RenderType.endPortal());
	}

	@Inject(method = "renderLevel", at = @At(value = "INVOKE", shift = At.Shift.AFTER, ordinal = 6, target = "Lnet/minecraft/client/renderer/MultiBufferSource$BufferSource;endBatch(Lnet/minecraft/client/renderer/RenderType;)V"))
	private void renderLayer$endGateway(CallbackInfo ci) {
		flywheel$dispatch(RenderType.endGateway());
	}

	@Inject(method = "renderLevel", at = @At(value = "INVOKE", shift = At.Shift.AFTER, ordinal = 7, target = "Lnet/minecraft/client/renderer/MultiBufferSource$BufferSource;endBatch(Lnet/minecraft/client/renderer/RenderType;)V"))
	private void renderLayer$solidBlockSheet(CallbackInfo ci) {
		flywheel$dispatch(Sheets.solidBlockSheet());
	}

	@Inject(method = "renderLevel", at = @At(value = "INVOKE", shift = At.Shift.AFTER, ordinal = 8, target = "Lnet/minecraft/client/renderer/MultiBufferSource$BufferSource;endBatch(Lnet/minecraft/client/renderer/RenderType;)V"))
	private void renderLayer$cutoutBlockSheet(CallbackInfo ci) {
		flywheel$dispatch(Sheets.cutoutBlockSheet());
	}

	@Inject(method = "renderLevel", at = @At(value = "INVOKE", shift = At.Shift.AFTER, ordinal = 9, target = "Lnet/minecraft/client/renderer/MultiBufferSource$BufferSource;endBatch(Lnet/minecraft/client/renderer/RenderType;)V"))
	private void renderLayer$bedSheet(CallbackInfo ci) {
		flywheel$dispatch(Sheets.bedSheet());
	}

	@Inject(method = "renderLevel", at = @At(value = "INVOKE", shift = At.Shift.AFTER, ordinal = 10, target = "Lnet/minecraft/client/renderer/MultiBufferSource$BufferSource;endBatch(Lnet/minecraft/client/renderer/RenderType;)V"))
	private void renderLayer$shulkerBoxSheet(CallbackInfo ci) {
		flywheel$dispatch(Sheets.shulkerBoxSheet());
	}

	@Inject(method = "renderLevel", at = @At(value = "INVOKE", shift = At.Shift.AFTER, ordinal = 11, target = "Lnet/minecraft/client/renderer/MultiBufferSource$BufferSource;endBatch(Lnet/minecraft/client/renderer/RenderType;)V"))
	private void renderLayer$signSheet(CallbackInfo ci) {
		flywheel$dispatch(Sheets.signSheet());
	}

	@Inject(method = "renderLevel", at = @At(value = "INVOKE", shift = At.Shift.AFTER, ordinal = 12, target = "Lnet/minecraft/client/renderer/MultiBufferSource$BufferSource;endBatch(Lnet/minecraft/client/renderer/RenderType;)V"))
	private void renderLayer$chestSheet(CallbackInfo ci) {
		flywheel$dispatch(Sheets.chestSheet());
	}

	@Inject(method = "renderLevel", at = @At(value = "INVOKE", shift = At.Shift.AFTER, ordinal = 13, target = "Lnet/minecraft/client/renderer/MultiBufferSource$BufferSource;endBatch(Lnet/minecraft/client/renderer/RenderType;)V"))
	private void renderLayer$translucentCullBlockSheet(CallbackInfo ci) {
		flywheel$dispatch(Sheets.translucentCullBlockSheet());
	}

	@Inject(method = "renderLevel", at = @At(value = "INVOKE", shift = At.Shift.AFTER, ordinal = 14, target = "Lnet/minecraft/client/renderer/MultiBufferSource$BufferSource;endBatch(Lnet/minecraft/client/renderer/RenderType;)V"))
	private void renderLayer$bannerSheet(CallbackInfo ci) {
		flywheel$dispatch(Sheets.bannerSheet());
	}

	@Inject(method = "renderLevel", at = @At(value = "INVOKE", shift = At.Shift.AFTER, ordinal = 15, target = "Lnet/minecraft/client/renderer/MultiBufferSource$BufferSource;endBatch(Lnet/minecraft/client/renderer/RenderType;)V"))
	private void renderLayer$shieldSheet(CallbackInfo ci) {
		flywheel$dispatch(Sheets.shieldSheet());
	}

	@Inject(method = "renderLevel", at = @At(value = "INVOKE", shift = At.Shift.AFTER, ordinal = 16, target = "Lnet/minecraft/client/renderer/MultiBufferSource$BufferSource;endBatch(Lnet/minecraft/client/renderer/RenderType;)V"))
	private void renderLayer$armorGlint(CallbackInfo ci) {
		flywheel$dispatch(RenderType.armorGlint());
	}

	@Inject(method = "renderLevel", at = @At(value = "INVOKE", shift = At.Shift.AFTER, ordinal = 17, target = "Lnet/minecraft/client/renderer/MultiBufferSource$BufferSource;endBatch(Lnet/minecraft/client/renderer/RenderType;)V"))
	private void renderLayer$armorEntityGlint(CallbackInfo ci) {
		flywheel$dispatch(RenderType.armorEntityGlint());
	}

	@Inject(method = "renderLevel", at = @At(value = "INVOKE", shift = At.Shift.AFTER, ordinal = 18, target = "Lnet/minecraft/client/renderer/MultiBufferSource$BufferSource;endBatch(Lnet/minecraft/client/renderer/RenderType;)V"))
	private void renderLayer$glint(CallbackInfo ci) {
		flywheel$dispatch(RenderType.glint());
	}

	@Inject(method = "renderLevel", at = @At(value = "INVOKE", shift = At.Shift.AFTER, ordinal = 19, target = "Lnet/minecraft/client/renderer/MultiBufferSource$BufferSource;endBatch(Lnet/minecraft/client/renderer/RenderType;)V"))
	private void renderLayer$glintDirect(CallbackInfo ci) {
		flywheel$dispatch(RenderType.glintDirect());
	}

	@Inject(method = "renderLevel", at = @At(value = "INVOKE", shift = At.Shift.AFTER, ordinal = 20, target = "Lnet/minecraft/client/renderer/MultiBufferSource$BufferSource;endBatch(Lnet/minecraft/client/renderer/RenderType;)V"))
	private void renderLayer$glintTranslucent(CallbackInfo ci) {
		flywheel$dispatch(RenderType.glintTranslucent());
	}

	@Inject(method = "renderLevel", at = @At(value = "INVOKE", shift = At.Shift.AFTER, ordinal = 21, target = "Lnet/minecraft/client/renderer/MultiBufferSource$BufferSource;endBatch(Lnet/minecraft/client/renderer/RenderType;)V"))
	private void renderLayer$entityGlint(CallbackInfo ci) {
		flywheel$dispatch(RenderType.entityGlint());
	}

	@Inject(method = "renderLevel", at = @At(value = "INVOKE", shift = At.Shift.AFTER, ordinal = 22, target = "Lnet/minecraft/client/renderer/MultiBufferSource$BufferSource;endBatch(Lnet/minecraft/client/renderer/RenderType;)V"))
	private void renderLayer$entityGlintDirect(CallbackInfo ci) {
		flywheel$dispatch(RenderType.entityGlintDirect());
	}

	@Inject(method = "renderLevel", at = @At(value = "INVOKE", shift = At.Shift.AFTER, ordinal = 23, target = "Lnet/minecraft/client/renderer/MultiBufferSource$BufferSource;endBatch(Lnet/minecraft/client/renderer/RenderType;)V"))
	private void renderLayer$waterMask(CallbackInfo ci) {
		flywheel$dispatch(RenderType.waterMask());
	}

	@Inject(method = "renderLevel", at = @At(value = "INVOKE", shift = At.Shift.AFTER, ordinal = 24, target = "Lnet/minecraft/client/renderer/MultiBufferSource$BufferSource;endBatch(Lnet/minecraft/client/renderer/RenderType;)V"))
	private void renderLayer$lines1(CallbackInfo ci) {
		flywheel$dispatch(RenderType.lines());
	}

	@Inject(method = "renderLevel", at = @At(value = "INVOKE", shift = At.Shift.AFTER, ordinal = 25, target = "Lnet/minecraft/client/renderer/MultiBufferSource$BufferSource;endBatch(Lnet/minecraft/client/renderer/RenderType;)V"))
	private void renderLayer$lines2(CallbackInfo ci) {
		flywheel$dispatch(RenderType.lines());
	}
}
