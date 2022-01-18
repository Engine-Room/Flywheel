package com.jozufozu.flywheel.mixin;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Group;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.jozufozu.flywheel.backend.Backend;
import com.jozufozu.flywheel.backend.gl.GlStateTracker;
import com.jozufozu.flywheel.backend.instancing.InstancedRenderDispatcher;
import com.jozufozu.flywheel.core.crumbling.CrumblingRenderer;
import com.jozufozu.flywheel.event.BeginFrameEvent;
import com.jozufozu.flywheel.event.ReloadRenderersEvent;
import com.jozufozu.flywheel.event.RenderLayerEvent;
import com.mojang.blaze3d.vertex.BufferUploader;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Matrix4f;

import net.minecraft.client.Camera;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.RenderBuffers;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.MinecraftForge;

@OnlyIn(Dist.CLIENT)
@Mixin(value = LevelRenderer.class, priority = 1001) // Higher priority to go after sodium
public class LevelRendererMixin {

	@Shadow
	private ClientLevel level;

	@Shadow
	@Final
	private RenderBuffers renderBuffers;

	@Inject(at = @At("HEAD"), method = "setupRender")
	private void setupRender(Camera camera, Frustum frustum, boolean queue, boolean isSpectator, CallbackInfo ci) {
		MinecraftForge.EVENT_BUS.post(new BeginFrameEvent(level, camera, frustum));
	}

	@Unique
	private boolean flywheel$LayerRendered;

	/**
	 * This only gets injected if renderChunkLayer is not Overwritten
	 */
	@Group(name = "flywheel$renderLayer", min = 1, max = 2)
	@Inject(at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/ShaderInstance;clear()V"), method = "renderChunkLayer")
	private void renderLayer(RenderType type, PoseStack stack, double camX, double camY, double camZ, Matrix4f p_172999_, CallbackInfo ci) {
		flywheel$renderLayer(type, stack, camX, camY, camZ);
		flywheel$LayerRendered = true;
	}

	/**
	 * This always gets injected.
	 */
	@Group(name = "flywheel$renderLayer")
	@Inject(at = @At("TAIL"), method = "renderChunkLayer")
	private void renderLayerSodium(RenderType type, PoseStack stack, double camX, double camY, double camZ, Matrix4f p_172999_, CallbackInfo ci) {
		if (!flywheel$LayerRendered) {
			flywheel$renderLayer(type, stack, camX, camY, camZ);
		}
		flywheel$LayerRendered = false;
		BufferUploader.reset();
	}

	@Unique
	private void flywheel$renderLayer(RenderType type, PoseStack stack, double camX, double camY, double camZ) {
		RenderBuffers renderBuffers = this.renderBuffers;

		GlStateTracker.State restoreState = GlStateTracker.getRestoreState();

		MinecraftForge.EVENT_BUS.post(new RenderLayerEvent(level, type, stack, renderBuffers, camX, camY, camZ));

		restoreState.restore();
	}

	@Inject(at = @At("TAIL"), method = "allChanged")
	private void refresh(CallbackInfo ci) {
		Backend.getInstance()
				.refresh();

		MinecraftForge.EVENT_BUS.post(new ReloadRenderersEvent(level));
	}


	@Inject(at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/LevelRenderer;checkPoseStack(Lcom/mojang/blaze3d/vertex/PoseStack;)V", ordinal = 2 // after the game renders the breaking overlay normally
	), method = "renderLevel")
	private void renderBlockBreaking(PoseStack stack, float p_228426_2_, long p_228426_3_, boolean p_228426_5_, Camera info, GameRenderer gameRenderer, LightTexture lightTexture, Matrix4f p_228426_9_, CallbackInfo ci) {
		if (!Backend.isOn()) return;

		Vec3 cameraPos = info.getPosition();

		GlStateTracker.State restoreState = GlStateTracker.getRestoreState();
		CrumblingRenderer.renderBreaking(new RenderLayerEvent(level, null, stack, null, cameraPos.x, cameraPos.y, cameraPos.z));
		restoreState.restore();
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
