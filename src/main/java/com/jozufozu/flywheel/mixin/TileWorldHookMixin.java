package com.jozufozu.flywheel.mixin;

import java.util.Iterator;
import java.util.List;
import java.util.Set;

import net.minecraft.core.BlockPos;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.level.Level;

import net.minecraft.world.level.block.entity.BlockEntity;

import net.minecraft.world.level.block.entity.TickingBlockEntity;
import net.minecraft.world.level.chunk.LevelChunk;

import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.jozufozu.flywheel.backend.instancing.InstanceManager;
import com.jozufozu.flywheel.backend.instancing.InstancedRenderDispatcher;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

@Environment(EnvType.CLIENT)
@Mixin(value = Level.class, priority = 1100) // this and create.mixins.json have high priority to load after Performant
public abstract class TileWorldHookMixin {

	final Level self = (Level) (Object) this;

	@Shadow
	@Final
	public boolean isClientSide;

	@Shadow
	@Nullable
	public abstract BlockEntity getBlockEntity(BlockPos blockPos);

	/**
	 * Without this we don't unload instances when a chunk unloads.
	 */
	@Inject(at = @At(value = "INVOKE", target = "Ljava/util/Iterator;remove()V", ordinal = 0), method = "tickBlockEntities", locals = LocalCapture.CAPTURE_FAILHARD)
	private void onChunkUnload(CallbackInfo ci, ProfilerFiller profilerFiller, Iterator<TickingBlockEntity> iterator, TickingBlockEntity tickingBlockEntity) {
		if (isClientSide) {
			BlockEntity be = getBlockEntity(tickingBlockEntity.getPos());
			InstancedRenderDispatcher.getTiles(self).remove(be);
		}
	}
}
