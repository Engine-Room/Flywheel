package com.jozufozu.flywheel.mixin.fabric;

import net.minecraft.client.multiplayer.ClientChunkCache;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(ClientChunkCache.class)
public interface ClientChunkCacheAccessor {
	@Accessor("storage")
	ClientChunkCache.Storage getStorage();
}
