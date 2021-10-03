package com.jozufozu.flywheel.mixin;

import java.util.concurrent.locks.StampedLock;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.jozufozu.flywheel.backend.Backend;

import net.minecraft.client.multiplayer.ClientChunkProvider;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.biome.BiomeContainer;
import net.minecraft.world.chunk.AbstractChunkProvider;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.ChunkStatus;
import net.minecraft.world.chunk.IChunk;

@Mixin(ClientChunkProvider.class)
public abstract class FastChunkProviderMixin extends AbstractChunkProvider {

	@Unique
	private final StampedLock lastChunkLock = new StampedLock();
	@Unique
	private volatile long lastChunkPos;
	@Unique
	private volatile IChunk lastChunk;

	@Inject(method = "getChunk",
			at = @At("HEAD"),
			cancellable = true)
	public void returnCachedChunk(int x, int z, ChunkStatus status, boolean create, CallbackInfoReturnable<IChunk> cir) {
		if (Backend.getInstance().chunkCachingEnabled && status.isOrAfter(ChunkStatus.FULL)) {
			StampedLock lock = lastChunkLock;
			long stamp = lock.tryOptimisticRead();
			if (stamp != 0) {
				if (ChunkPos.asLong(x, z) == lastChunkPos) {
					IChunk chunk = lastChunk;
					if (chunk != null && lock.validate(stamp)) {
						cir.setReturnValue(chunk);
					}
				}
			}
		}
	}

	@Inject(method = "getChunk",
			at = @At("RETURN"))
	public void cacheChunk(int x, int z, ChunkStatus status, boolean create, CallbackInfoReturnable<IChunk> cir) {
		if (Backend.getInstance().chunkCachingEnabled && status.isOrAfter(ChunkStatus.FULL)) {
			StampedLock lock = lastChunkLock;
			long stamp = lock.tryWriteLock();
			if (stamp != 0) {
				lastChunkPos = ChunkPos.asLong(x, z);
				lastChunk = cir.getReturnValue();
				lock.unlockWrite(stamp);
			}
		}
	}

	@Inject(method = "drop", at = @At("HEAD"))
	public void invalidateOnDrop(int x, int z, CallbackInfo ci) {
		if (Backend.getInstance().chunkCachingEnabled) {
			StampedLock lock = lastChunkLock;
			long stamp = lock.writeLock();
			if (ChunkPos.asLong(x, z) == lastChunkPos) {
				lastChunk = null;
			}
			lock.unlockWrite(stamp);
		}
	}

	@Inject(method = "replaceWithPacketData", at = @At("HEAD"))
	public void invalidateOnPacket(int x, int z, BiomeContainer p_228313_3_, PacketBuffer p_228313_4_, CompoundNBT p_228313_5_, int p_228313_6_, boolean p_228313_7_, CallbackInfoReturnable<Chunk> cir) {
		if (Backend.getInstance().chunkCachingEnabled) {
			StampedLock lock = lastChunkLock;
			long stamp = lock.writeLock();
			if (ChunkPos.asLong(x, z) == lastChunkPos) {
				lastChunk = null;
			}
			lock.unlockWrite(stamp);
		}
	}

	@Redirect(method = "isTickingChunk", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/multiplayer/ClientChunkProvider;hasChunk(II)Z"))
	public boolean redirectTicking(ClientChunkProvider clientChunkProvider, int x, int z) {
		if (Backend.getInstance().chunkCachingEnabled) {
			StampedLock lock = lastChunkLock;
			long stamp = lock.tryOptimisticRead();
			if (stamp != 0) {
				if (ChunkPos.asLong(x, z) == lastChunkPos && lastChunk != null) {
					if (lock.validate(stamp)) {
						return true;
					}
				}
			}
		}

		return clientChunkProvider.hasChunk(x, z);
	}
}
