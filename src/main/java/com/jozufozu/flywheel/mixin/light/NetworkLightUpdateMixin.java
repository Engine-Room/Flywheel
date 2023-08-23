package com.jozufozu.flywheel.mixin.light;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.jozufozu.flywheel.backend.RenderWork;
import com.jozufozu.flywheel.light.LightUpdater;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.network.protocol.game.ClientboundLevelChunkWithLightPacket;
import net.minecraft.network.protocol.game.ClientboundLightUpdatePacket;

@Mixin(ClientPacketListener.class)
public class NetworkLightUpdateMixin {
	@Shadow
	private ClientLevel level;

	@Inject(method = "handleLevelChunkWithLight(Lnet/minecraft/network/protocol/game/ClientboundLevelChunkWithLightPacket;)V", at = @At("TAIL"))
	private void flywheel$onLevelChunkWithLight(ClientboundLevelChunkWithLightPacket packet, CallbackInfo ci) {
		RenderWork.enqueue(() -> {
			ClientLevel level = this.level;

			if (level == null) return;

			int chunkX = packet.getX();
			int chunkZ = packet.getZ();

			LightUpdater.get(level)
					.onLightPacket(chunkX, chunkZ);
		});
	}

	@Inject(method = "handleLightUpdatePacket(Lnet/minecraft/network/protocol/game/ClientboundLightUpdatePacket;)V", at = @At("TAIL"))
	private void flywheel$onLightUpdatePacket(ClientboundLightUpdatePacket packet, CallbackInfo ci) {
		RenderWork.enqueue(() -> {
			ClientLevel level = this.level;

			if (level == null) return;

			int chunkX = packet.getX();
			int chunkZ = packet.getZ();

			LightUpdater.get(level)
					.onLightPacket(chunkX, chunkZ);
		});
	}
}
