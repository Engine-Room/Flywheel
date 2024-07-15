package dev.engine_room.flywheel.impl.mixin;

import java.util.SortedSet;

import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.At.Shift;
import org.spongepowered.asm.mixin.injection.Group;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.llamalad7.mixinextras.sugar.Share;
import com.llamalad7.mixinextras.sugar.ref.LocalRef;
import com.mojang.blaze3d.vertex.PoseStack;

import dev.engine_room.flywheel.api.event.RenderStage;
import dev.engine_room.flywheel.impl.FlwImplXplat;
import dev.engine_room.flywheel.impl.event.RenderContextImpl;
import dev.engine_room.flywheel.impl.visualization.VisualizationManagerImpl;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import net.minecraft.client.Camera;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.RenderBuffers;
import net.minecraft.server.level.BlockDestructionProgress;

@Mixin(value = LevelRenderer.class, priority = 1001) // Higher priority to go after Sodium
abstract class LevelRendererMixin {
	@Shadow
	@Nullable
	private ClientLevel level;

	@Shadow
	@Final
	private RenderBuffers renderBuffers;

	@Shadow
	@Final
	private Long2ObjectMap<SortedSet<BlockDestructionProgress>> destructionProgress;

	@Unique
	@Nullable
	private RenderContextImpl flywheel$renderContext;

	//	@Inject(method = "renderLevel", at = @At("HEAD"))
	@Inject(method = "renderLevel", at = @At(value = "INVOKE_ASSIGN", target = "Lnet/minecraft/world/level/lighting/LevelLightEngine;runLightUpdates()I"))
	private void flywheel$beginRender(DeltaTracker deltaTracker, boolean renderBlockOutline, Camera camera, GameRenderer gameRenderer, LightTexture lightTexture, Matrix4f frustumMatrix, Matrix4f projectionMatrix, CallbackInfo ci, @Share("flywheel$poseStack") LocalRef<PoseStack> poseStack) {
		PoseStack flywheel$poseStack = new PoseStack();
		flywheel$renderContext = RenderContextImpl.create((LevelRenderer) (Object) this, level, renderBuffers, flywheel$poseStack, projectionMatrix, camera, deltaTracker.getGameTimeDeltaTicks());

		FlwImplXplat.INSTANCE.dispatchBeginFrameEvent(flywheel$renderContext);
	}

	@Inject(method = "renderLevel", at = @At("RETURN"))
	private void flywheel$endRender(CallbackInfo ci) {
		flywheel$renderContext = null;
	}

	@Inject(method = "allChanged", at = @At("RETURN"))
	private void flywheel$refresh(CallbackInfo ci) {
		FlwImplXplat.INSTANCE.dispatchReloadLevelRendererEvent(level);
	}

	@Inject(method = "renderLevel", at = @At(value = "INVOKE_STRING", target = "Lnet/minecraft/util/profiling/ProfilerFiller;popPush(Ljava/lang/String;)V", args = "ldc=destroyProgress"))
	private void flywheel$beforeRenderCrumbling(CallbackInfo ci) {
		if (flywheel$renderContext == null) {
			return;
		}

		var manager = VisualizationManagerImpl.get(level);
		if (manager != null) {
			manager.renderCrumbling(flywheel$renderContext, destructionProgress);
		}
	}

	// STAGE DISPATCHING

	@Unique
	private void flywheel$dispatch(RenderStage stage) {
		if (flywheel$renderContext != null) {
			FlwImplXplat.INSTANCE.dispatchRenderStageEvent(flywheel$renderContext, stage);
		}
	}

	@Inject(method = "renderLevel", at = @At(value = "INVOKE_STRING", target = "Lnet/minecraft/util/profiling/ProfilerFiller;popPush(Ljava/lang/String;)V", args = "ldc=blockentities"))
	private void flywheel$onStage$beforeBlockEntities(CallbackInfo ci) {
		flywheel$dispatch(RenderStage.AFTER_ENTITIES);
	}

	@Inject(method = "renderLevel", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/OutlineBufferSource;endOutlineBatch()V", ordinal = 0))
	private void flywheel$onStage$afterSolidBlockEntities(CallbackInfo ci) {
		flywheel$dispatch(RenderStage.AFTER_BLOCK_ENTITIES);
	}

	@Inject(method = "renderLevel", at = @At(value = "FIELD", target = "Lnet/minecraft/client/renderer/LevelRenderer;particlesTarget:Lcom/mojang/blaze3d/pipeline/RenderTarget;", opcode = Opcodes.GETFIELD, ordinal = 0))
	private void flywheel$onStage$afterTranslucentTerrain$fabulous(CallbackInfo ci) {
		flywheel$dispatch(RenderStage.AFTER_TRANSLUCENT_TERRAIN);
	}

	@Inject(method = "renderLevel", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/LevelRenderer;renderSectionLayer(Lnet/minecraft/client/renderer/RenderType;DDDLorg/joml/Matrix4f;Lorg/joml/Matrix4f;)V", ordinal = 6, shift = Shift.AFTER))
	private void flywheel$onStage$afterTranslucentTerrain(CallbackInfo ci) {
		flywheel$dispatch(RenderStage.AFTER_TRANSLUCENT_TERRAIN);
	}

	@Group(name = "onStage$afterParticles", min = 2, max = 3)
	@Inject(method = "renderLevel", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/particle/ParticleEngine;render(Lnet/minecraft/client/renderer/LightTexture;Lnet/minecraft/client/Camera;F)V", shift = Shift.AFTER))
	private void flywheel$onStage$afterParticles$fabric(CallbackInfo ci) {
		flywheel$dispatch(RenderStage.AFTER_PARTICLES);
	}

	@Group(name = "onStage$afterParticles")
	@Inject(method = "renderLevel", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/particle/ParticleEngine;render(Lnet/minecraft/client/renderer/LightTexture;Lnet/minecraft/client/Camera;FLnet/minecraft/client/renderer/culling/Frustum;Ljava/util/function/Predicate;)V", shift = Shift.AFTER))
	private void flywheel$onStage$afterParticles$forge(CallbackInfo ci) {
		flywheel$dispatch(RenderStage.AFTER_PARTICLES);
	}

	@Inject(method = "renderLevel", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/LevelRenderer;renderSnowAndRain(Lnet/minecraft/client/renderer/LightTexture;FDDD)V", shift = Shift.AFTER))
	private void flywheel$onStage$afterWeather(CallbackInfo ci) {
		flywheel$dispatch(RenderStage.AFTER_WEATHER);
	}
}
