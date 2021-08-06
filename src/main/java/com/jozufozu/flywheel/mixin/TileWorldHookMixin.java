package com.jozufozu.flywheel.mixin;

import java.util.List;
import java.util.Set;

import net.minecraft.world.level.Level;

import net.minecraft.world.level.block.entity.BlockEntity;

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

@Environment(EnvType.CLIENT)
@Mixin(value = Level.class, priority = 1100) // this and create.mixins.json have high priority to load after Performant
public class TileWorldHookMixin {

	final Level self = (Level) (Object) this;

	@Shadow
	@Final
	public boolean isClientSide;

	@Shadow
	@Final
	protected Set<BlockEntity> blockEntitiesToUnload;

	@Inject(at = @At("TAIL"), method = "addBlockEntity")
	private void onAddTile(BlockEntity te, CallbackInfoReturnable<Boolean> cir) {
		if (isClientSide) {
			InstancedRenderDispatcher.getTiles(self)
					.queueAdd(te);
		}
	}

	/**
	 * Without this we don't unload instances when a chunk unloads.
	 */
	@Inject(at = @At(value = "INVOKE", target = "Ljava/util/List;clear()V", ordinal = 0), method = "tickBlockEntities")
	private void onChunkUnload(CallbackInfo ci) {
		if (isClientSide) {
			InstanceManager<BlockEntity> kineticRenderer = InstancedRenderDispatcher.getTiles(self);
			for (BlockEntity tile : blockEntitiesToUnload) {
				kineticRenderer.remove(tile);
			}
		}
	}
}
