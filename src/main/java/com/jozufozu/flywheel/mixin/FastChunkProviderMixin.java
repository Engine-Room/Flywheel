package com.jozufozu.flywheel.mixin;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.jozufozu.flywheel.backend.Backend;

import net.minecraft.client.multiplayer.ClientChunkCache;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.level.chunk.ChunkBiomeContainer;
import net.minecraft.world.level.chunk.ChunkSource;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.chunk.ChunkStatus;
import net.minecraft.world.level.chunk.ChunkAccess;

import java.util.BitSet;

@Mixin(ClientChunkCache.class)
public abstract class FastChunkProviderMixin extends ChunkSource {

	@Shadow
	@Final
	private ClientLevel level;
	@Unique
	private int lastX;
	@Unique
	private int lastZ;

	@Unique
	private ChunkAccess lastChunk;

	@Inject(method = "getChunk",
			at = @At("HEAD"),
			cancellable = true)
	public void returnCachedChunk(int x, int z, ChunkStatus status, boolean create, CallbackInfoReturnable<ChunkAccess> cir) {
		if (Backend.getInstance().chunkCachingEnabled && status.isOrAfter(ChunkStatus.FULL)) {
			synchronized (level) {
				if (lastChunk != null && x == lastX && z == lastZ) {
					cir.setReturnValue(lastChunk);
				}
			}
		}
	}

	@Inject(method = "getChunk",
			at = @At("RETURN"))
	public void cacheChunk(int x, int z, ChunkStatus status, boolean create, CallbackInfoReturnable<ChunkAccess> cir) {
		if (Backend.getInstance().chunkCachingEnabled && status.isOrAfter(ChunkStatus.FULL)) {
			synchronized (level) {
				lastChunk = cir.getReturnValue();
				lastX = x;
				lastZ = z;
			}
		}
	}

	@Inject(method = "drop", at = @At("HEAD"))
	public void invalidateOnDrop(int x, int z, CallbackInfo ci) {
		if (Backend.getInstance().chunkCachingEnabled) {
			synchronized (level) {
				if (x == lastX && z == lastZ) lastChunk = null;
			}
		}
	}

	@Inject(method = "replaceWithPacketData", at = @At("HEAD"))
	public void invalidateOnPacket(int x, int z, ChunkBiomeContainer p_171618_, FriendlyByteBuf p_171619_, CompoundTag p_171620_, BitSet p_171621_, CallbackInfoReturnable<LevelChunk> cir) {
		if (Backend.getInstance().chunkCachingEnabled) {
			synchronized (level) {
				if (x == lastX && z == lastZ) lastChunk = null;
			}
		}
	}

	@Redirect(method = "isTickingChunk", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/multiplayer/ClientChunkProvider;hasChunk(II)Z"))
	public boolean redirectTicking(ClientChunkCache clientChunkProvider, int x, int z) {
		if (Backend.getInstance().chunkCachingEnabled) {
			synchronized (level) {
				if (lastChunk != null && x == lastX && z == lastZ) return true;
			}
		}

		return clientChunkProvider.hasChunk(x, z);
	}
}
