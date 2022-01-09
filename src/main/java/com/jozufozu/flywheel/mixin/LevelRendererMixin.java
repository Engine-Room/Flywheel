package com.jozufozu.flywheel.mixin;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.jozufozu.flywheel.backend.Backend;
import com.jozufozu.flywheel.backend.gl.GlStateTracker;
import com.jozufozu.flywheel.backend.instancing.InstancedRenderDispatcher;
import com.jozufozu.flywheel.core.crumbling.CrumblingRenderer;
import com.jozufozu.flywheel.event.BeginFrameEvent;
import com.jozufozu.flywheel.event.ReloadRenderersEvent;
import com.jozufozu.flywheel.event.RenderLayerEvent;
import com.jozufozu.flywheel.fabric.event.FlywheelEvents;
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

@Mixin(LevelRenderer.class)
public class LevelRendererMixin {

	@Shadow
	private ClientLevel level;

	@Shadow
	@Final
	private RenderBuffers renderBuffers;

	@Inject(at = @At("HEAD"), method = "setupRender")
	private void setupRender(Camera camera, Frustum frustum, boolean queue, boolean isSpectator, CallbackInfo ci) {
		FlywheelEvents.BEGIN_FRAME.invoker().handleEvent(new BeginFrameEvent(level, camera, frustum));
	}

	/**
	 * JUSTIFICATION: This method is called once per layer per frame. It allows us to perform
	 * layer-correct custom rendering. RenderWorldLast is not refined enough for rendering world objects.
	 * This should probably be a forge event.
	 */
	@Inject(at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/ShaderInstance;clear()V"), method = "renderChunkLayer")
	private void renderLayer(RenderType type, PoseStack stack, double camX, double camY, double camZ, Matrix4f p_172999_, CallbackInfo ci) {

		RenderBuffers renderBuffers = this.renderBuffers;

		GlStateTracker.State restoreState = GlStateTracker.getRestoreState();

		FlywheelEvents.RENDER_LAYER.invoker().handleEvent(new RenderLayerEvent(level, type, stack, renderBuffers, camX, camY, camZ));

		restoreState.restore();
	}

	@Inject(at = @At("TAIL"), method = "allChanged")
	private void refresh(CallbackInfo ci) {
		Backend.getInstance()
				.refresh();

		FlywheelEvents.RELOAD_RENDERERS.invoker().handleEvent(new ReloadRenderersEvent(level));
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
