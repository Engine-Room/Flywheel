package com.jozufozu.flywheel.mixin.instancemanage;

import java.util.ArrayList;
import java.util.List;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.jozufozu.flywheel.backend.Backend;
import com.jozufozu.flywheel.backend.instancing.InstancedRenderDispatcher;
import com.jozufozu.flywheel.backend.instancing.InstancedRenderRegistry;

import me.jellysquid.mods.sodium.client.render.chunk.data.ChunkRenderData;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
@Mixin(targets = "me.jellysquid.mods.sodium.client.render.chunk.data.ChunkRenderData$Builder", remap = false)
public class SodiumChunkRenderDataMixin {

	@Unique
	private List<BlockEntity> flywheel$blockEntities;
	@Unique
	private Level flywheel$level;

	@Inject(method = "addBlockEntity", at = @At("HEAD"), cancellable = true, require = 0)
	private void flywheel$onAddBlockEntity(BlockEntity be, boolean cull, CallbackInfo ci) {
		if (flywheel$level == null) {
			flywheel$level = be.getLevel();
		}

		if (!Backend.canUseInstancing(flywheel$level)) {
			return;
		}

		if (InstancedRenderRegistry.canInstance(be.getType())) {
			if (flywheel$blockEntities == null) {
				flywheel$blockEntities = new ArrayList<>();
			}

			// Collect BEs in a temporary list to avoid excessive synchronization in InstancedRenderDispatcher.
			flywheel$blockEntities.add(be);
		}

		if (InstancedRenderRegistry.shouldSkipRender(be)) {
			ci.cancel();
		}
	}

	@Inject(method = "build", at = @At("HEAD"))
	private void flywheel$onBuild(CallbackInfoReturnable<ChunkRenderData> cir) {
		if (flywheel$level == null || flywheel$blockEntities == null || !Backend.canUseInstancing(flywheel$level)) {
			return;
		}

		InstancedRenderDispatcher.getBlockEntities(flywheel$level)
				.queueAddAll(flywheel$blockEntities);
	}
}
