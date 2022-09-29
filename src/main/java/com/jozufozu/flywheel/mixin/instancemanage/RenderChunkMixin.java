package com.jozufozu.flywheel.mixin.instancemanage;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.chunk.ChunkRenderDispatcher;

@Mixin(ChunkRenderDispatcher.RenderChunk.class)
public class RenderChunkMixin implements com.jozufozu.flywheel.util.RenderChunkExtension {

	@Shadow(aliases = "this$0") // Optifine does not use the obfuscated name so the mapped name must be included as an alias
	@Final
	private ChunkRenderDispatcher this$0;

	@Override
	public ClientLevel flywheel$getLevel() {
		return ((ChunkRenderDispatcherAccessor) this$0).getLevel();
	}
}
