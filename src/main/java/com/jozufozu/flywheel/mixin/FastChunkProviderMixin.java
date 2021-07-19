package com.jozufozu.flywheel.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.client.multiplayer.ClientChunkProvider;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.PacketBuffer;
import net.minecraft.world.biome.BiomeContainer;
import net.minecraft.world.chunk.AbstractChunkProvider;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.ChunkStatus;
import net.minecraft.world.chunk.IChunk;

@Mixin(ClientChunkProvider.class)
public abstract class FastChunkProviderMixin extends AbstractChunkProvider {

	@Unique
	private int lastX;
	@Unique
	private int lastZ;

	@Unique
	private IChunk lastChunk;

	@Inject(method = "getChunk",
			at = @At("HEAD"),
			cancellable = true)
	public void returnCachedChunk(int x, int z, ChunkStatus status, boolean create, CallbackInfoReturnable<IChunk> cir) {
		if (status.isOrAfter(ChunkStatus.FULL) && lastChunk != null && x == lastX && z == lastZ) {
			cir.setReturnValue(lastChunk);
		}
	}

	@Inject(method = "getChunk",
			at = @At("RETURN"))
	public void cacheChunk(int x, int z, ChunkStatus status, boolean create, CallbackInfoReturnable<IChunk> cir) {
		if (status.isOrAfter(ChunkStatus.FULL)) {
			lastChunk = cir.getReturnValue();
			lastX = x;
			lastZ = z;
		}
	}

	@Inject(method = "drop", at = @At("HEAD"))
	public void invalidateOnDrop(int x, int z, CallbackInfo ci) {
		if (x == lastX && z == lastZ)
			lastChunk = null;
	}

	@Inject(method = "replaceWithPacketData", at = @At("HEAD"))
	public void invalidateOnPacket(int x, int z, BiomeContainer p_228313_3_, PacketBuffer p_228313_4_, CompoundNBT p_228313_5_, int p_228313_6_, boolean p_228313_7_, CallbackInfoReturnable<Chunk> cir) {
		if (x == lastX && z == lastZ)
			lastChunk = null;
	}

	@Redirect(method = "isTickingChunk", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/multiplayer/ClientChunkProvider;hasChunk(II)Z"))
	public boolean redirectTicking(ClientChunkProvider clientChunkProvider, int x, int z) {
		if (lastChunk != null && x == lastX && z == lastZ) return true;

		return clientChunkProvider.hasChunk(x, z);
	}
}
