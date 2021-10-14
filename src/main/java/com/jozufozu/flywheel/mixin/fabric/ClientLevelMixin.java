package com.jozufozu.flywheel.mixin.fabric;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.jozufozu.flywheel.event.ForgeEvents;

import net.minecraft.client.multiplayer.ClientLevel;

@Mixin(ClientLevel.class)
public class ClientLevelMixin {
	@Inject(method = "<init>(Lnet/minecraft/client/multiplayer/ClientPacketListener;Lnet/minecraft/client/multiplayer/ClientLevel$ClientLevelData;Lnet/minecraft/resources/ResourceKey;Lnet/minecraft/world/level/dimension/DimensionType;ILjava/util/function/Supplier;Lnet/minecraft/client/renderer/LevelRenderer;ZJ)V", at = @At("TAIL"))
	private void onTailInit(CallbackInfo ci) {
		ForgeEvents.onLoadWorld((ClientLevel) (Object) this);
	}
}
