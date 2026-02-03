package dev.engine_room.flywheel.impl.mixin;

import java.util.Iterator;
import java.util.SortedSet;

import org.joml.Matrix4f;
import org.joml.Vector4f;
import org.jspecify.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.google.common.collect.Iterators;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.mojang.blaze3d.buffers.GpuBufferSlice;
import com.mojang.blaze3d.resource.GraphicsResourceAllocator;

import dev.engine_room.flywheel.api.visualization.VisualizationManager;
import dev.engine_room.flywheel.impl.FlwImplXplat;
import dev.engine_room.flywheel.impl.event.RenderContextImpl;
import dev.engine_room.flywheel.lib.visualization.VisualizationHelper;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import net.minecraft.client.Camera;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.RenderBuffers;
import net.minecraft.server.level.BlockDestructionProgress;
import net.minecraft.world.entity.Entity;

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
	private void flywheel$beginRender(GraphicsResourceAllocator resourceAllocator, DeltaTracker deltaTracker, boolean renderOutline, Camera camera, Matrix4f modelViewMatrix, Matrix4f projectionMatrix, Matrix4f cullingProjectionMatrix, GpuBufferSlice shaderFog, Vector4f fogColor, boolean renderSky, CallbackInfo ci) {
		flywheel$renderContext = RenderContextImpl.create((LevelRenderer) (Object) this, level, renderBuffers, modelViewMatrix, projectionMatrix, camera, deltaTracker.getGameTimeDeltaPartialTick(false));

		VisualizationManager manager = VisualizationManager.get(level);
		if (manager != null) {
			manager.renderDispatcher().onStartLevelRender(flywheel$renderContext);
		}
	}

	@Inject(method = "renderLevel", at = @At("RETURN"))
	private void flywheel$endRender(CallbackInfo ci) {
		flywheel$renderContext = null;
	}

	@Inject(method = "allChanged", at = @At("RETURN"))
	private void flywheel$reload(CallbackInfo ci) {
		if (level != null) {
			FlwImplXplat.INSTANCE.dispatchReloadLevelRendererEvent(level);
		}
	}

	@Inject(method = "method_62214", at = @At(value = "INVOKE_STRING", target = "Lnet/minecraft/util/profiling/ProfilerFiller;popPush(Ljava/lang/String;)V", args = "ldc=submitBlockEntities"))
	private void flywheel$beforeBlockEntities(CallbackInfo ci) {
		if (flywheel$renderContext != null) {
			VisualizationManager manager = VisualizationManager.get(level);
			if (manager != null) {
				manager.renderDispatcher().afterEntities(flywheel$renderContext);
			}
		}
	}

	@Inject(method = "method_62214", at = @At(value = "INVOKE_STRING", target = "Lnet/minecraft/util/profiling/ProfilerFiller;push(Ljava/lang/String;)V", args = "ldc=destroyProgress"))
	private void flywheel$beforeRenderCrumbling(CallbackInfo ci) {
		if (flywheel$renderContext != null) {
			VisualizationManager manager = VisualizationManager.get(level);
			if (manager != null) {
				manager.renderDispatcher().beforeCrumbling(flywheel$renderContext, destructionProgress);
			}
		}
	}

	@ModifyExpressionValue(method = "extractVisibleEntities", at = @At(value = "INVOKE", target = "Ljava/lang/Iterable;iterator()Ljava/util/Iterator;"))
	private Iterator<Entity> flywheel$decideNotToRenderEntity(Iterator<Entity> original) {
		return Iterators.filter(original, entity -> !(VisualizationManager.supportsVisualization(entity.level()) &&
				VisualizationHelper.skipVanillaRender(entity)));
	}
}
