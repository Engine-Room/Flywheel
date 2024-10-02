package dev.engine_room.flywheel.backend.mixin;

import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.client.player.AbstractClientPlayer;

@Mixin(AbstractClientPlayer.class)
public interface AbstractClientPlayerAccessor {
	@Invoker("getPlayerInfo")
	@Nullable
	PlayerInfo flywheel$getPlayerInfo();
}
