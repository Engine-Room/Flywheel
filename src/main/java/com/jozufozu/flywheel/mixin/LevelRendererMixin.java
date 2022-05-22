package com.jozufozu.flywheel.mixin;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.At.Shift;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.jozufozu.flywheel.backend.Backend;
import com.jozufozu.flywheel.backend.gl.GlStateTracker;
import com.jozufozu.flywheel.backend.instancing.InstancedRenderDispatcher;
import com.jozufozu.flywheel.core.RenderContext;
import com.jozufozu.flywheel.core.crumbling.CrumblingRenderer;
import com.jozufozu.flywheel.event.BeginFrameEvent;
import com.jozufozu.flywheel.event.ReloadRenderersEvent;
import com.jozufozu.flywheel.event.RenderLayerEvent;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Matrix4f;

import net.minecraft.client.Camera;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.RenderBuffers;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.MinecraftForge;

@Mixin(value = LevelRenderer.class, priority = 1001) // Higher priority to go after sodium
public class LevelRendererMixin {

	@Shadow
	private ClientLevel level;

	@Shadow
	@Final
	private RenderBuffers renderBuffers;

	@Inject(at = @At("HEAD"), method = "renderLevel")
	private void beginRender(PoseStack pPoseStack, float pPartialTick, long pFinishNanoTime, boolean pRenderBlockOutline, Camera pCamera, GameRenderer pGameRenderer, LightTexture pLightTexture, Matrix4f pProjectionMatrix, CallbackInfo ci) {
		Vec3 position = pCamera.getPosition();
		RenderContext.CURRENT = new RenderContext(level, pPoseStack, RenderLayerEvent.createViewProjection(pPoseStack), renderBuffers, position.x, position.y, position.z);

		GlStateTracker.State restoreState = GlStateTracker.getRestoreState();
		MinecraftForge.EVENT_BUS.post(new BeginFrameEvent(level, pCamera, null));
		restoreState.restore();
	}

	@Inject(at = @At("TAIL"), method = "renderChunkLayer")
	private void renderChunkLayer(RenderType pRenderType, PoseStack pPoseStack, double pCamX, double pCamY, double pCamZ, Matrix4f pProjectionMatrix, CallbackInfo ci) {
		GlStateTracker.State restoreState = GlStateTracker.getRestoreState();

		// TODO: Is this necessary?
		InstancedRenderDispatcher.renderSpecificType(RenderContext.CURRENT, pRenderType);
		MinecraftForge.EVENT_BUS.post(new RenderLayerEvent(RenderContext.CURRENT, pRenderType));

		restoreState.restore();
	}

	@Inject(at = @At("TAIL"), method = "renderLevel")
	private void endRender(PoseStack pPoseStack, float pPartialTick, long pFinishNanoTime, boolean pRenderBlockOutline, Camera pCamera, GameRenderer pGameRenderer, LightTexture pLightTexture, Matrix4f pProjectionMatrix, CallbackInfo ci) {
		RenderContext.CURRENT = null;
	}

	@Inject(at = @At("TAIL"), method = "allChanged")
	private void refresh(CallbackInfo ci) {
		Backend.refresh();

		MinecraftForge.EVENT_BUS.post(new ReloadRenderersEvent(level));
	}

	@Inject(at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/RenderBuffers;crumblingBufferSource()Lnet/minecraft/client/renderer/MultiBufferSource$BufferSource;", ordinal = 2, shift = Shift.BY, by = 2 // after the game renders the breaking overlay normally
	), method = "renderLevel")
	private void renderBlockBreaking(PoseStack poseStack, float partialTick, long finishNanoTime, boolean renderBlockOutline, Camera camera, GameRenderer gameRenderer, LightTexture lightTexture, Matrix4f projectionMatrix, CallbackInfo ci) {
		CrumblingRenderer.renderCrumbling((LevelRenderer) (Object) this, level, poseStack, camera, projectionMatrix);
	}

	// Instancing

	/**
	 * This gets called when a block is marked for rerender by vanilla.
	 */
	@Inject(at = @At("TAIL"), method = "setBlockDirty(Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/world/level/block/state/BlockState;)V")
	private void checkUpdate(BlockPos pos, BlockState lastState, BlockState newState, CallbackInfo ci) {
		if (Backend.isOn()) {
			InstancedRenderDispatcher.getBlockEntities(level)
					.update(level.getBlockEntity(pos));
		}
	}
}
