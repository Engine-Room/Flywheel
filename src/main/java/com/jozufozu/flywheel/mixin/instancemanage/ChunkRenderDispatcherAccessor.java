package com.jozufozu.flywheel.mixin.instancemanage;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.chunk.ChunkRenderDispatcher;

@Mixin(ChunkRenderDispatcher.class)
public interface ChunkRenderDispatcherAccessor {

	@Accessor
	ClientLevel getLevel();
}
