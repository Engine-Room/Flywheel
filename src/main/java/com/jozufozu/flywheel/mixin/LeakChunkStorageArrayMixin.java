package com.jozufozu.flywheel.mixin;

import java.util.concurrent.atomic.AtomicReferenceArray;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.jozufozu.flywheel.util.ChunkIter;

import net.minecraft.client.multiplayer.ClientChunkCache;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

/**
 * In order to iterate over all loaded chunks, we do something absolutely foul.
 *
 * <p>
 *     By stealing the reference to the backing array of the chunk storage when it is constructed, we gain 0 maintenance
 *     access to the full array of loaded chunks.
 * </p>
 */
@OnlyIn(Dist.CLIENT)
@Mixin(targets = "net.minecraft.client.multiplayer.ClientChunkCache$Storage")
public class LeakChunkStorageArrayMixin {

	@Shadow
	@Final
	AtomicReferenceArray<LevelChunk> chunks;

	@Inject(method = "<init>", at = @At("TAIL"))
	private void leakBackingArray(ClientChunkCache outer, int chunkRadius, CallbackInfo ci) {
		ChunkIter._putStorageReference(outer.getLevel(), chunks);
	}
}
