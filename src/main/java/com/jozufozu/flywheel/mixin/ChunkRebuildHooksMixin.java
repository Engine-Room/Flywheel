package com.jozufozu.flywheel.mixin;

import java.util.Set;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.jozufozu.flywheel.backend.Backend;
import com.jozufozu.flywheel.backend.instancing.InstancedRenderDispatcher;
import com.jozufozu.flywheel.backend.instancing.InstancedRenderRegistry;

import net.minecraft.client.renderer.chunk.ChunkRenderDispatcher;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
@Mixin(targets = "net.minecraft.client.renderer.chunk.ChunkRenderDispatcher$RenderChunk$RebuildTask")
public class ChunkRebuildHooksMixin {

	@Inject(method = "handleBlockEntity", at = @At("HEAD"), cancellable = true)
	private <E extends BlockEntity> void addAndFilterBEs(ChunkRenderDispatcher.CompiledChunk compiledChunk, Set<BlockEntity> set, E be, CallbackInfo ci) {

		if (Backend.getInstance().canUseInstancing() && Backend.isFlywheelWorld(be.getLevel())) {

			InstancedRenderRegistry registry = InstancedRenderRegistry.getInstance();
			if (registry.canInstance(be.getType()))
				InstancedRenderDispatcher.getTiles(be.getLevel()).queueAdd(be);

			if (registry.shouldSkipRender(be))
				ci.cancel();
		}
	}
}
