package com.jozufozu.flywheel.mixin.light;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.jozufozu.flywheel.backend.RenderWork;
import com.jozufozu.flywheel.light.LightUpdater;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.network.protocol.game.ClientboundLightUpdatePacket;

@Mixin(ClientPacketListener.class)
public class NetworkLightUpdateMixin {

	@Inject(at = @At("TAIL"), method = "handleLightUpdatePacked")
	private void onLightPacket(ClientboundLightUpdatePacket packet, CallbackInfo ci) {
		RenderWork.enqueue(() -> {
			ClientLevel world = Minecraft.getInstance().level;

			if (world == null) return;

			int chunkX = packet.getX();
			int chunkZ = packet.getZ();

			LightUpdater.get(world)
					.onLightPacket(chunkX, chunkZ);
		});
	}
}
