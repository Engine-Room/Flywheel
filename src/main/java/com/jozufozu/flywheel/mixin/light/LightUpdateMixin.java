package com.jozufozu.flywheel.mixin.light;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.jozufozu.flywheel.light.LightUpdater;

import net.minecraft.client.multiplayer.ClientChunkCache;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.SectionPos;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.chunk.ChunkSource;

@Mixin(ClientChunkCache.class)
public abstract class LightUpdateMixin extends ChunkSource {
	@Shadow
	@Final
	ClientLevel level;

	/**
	 * JUSTIFICATION: This method is called after a lighting tick once per subchunk where a
	 * lighting change occurred that tick. On the client, Minecraft uses this method to inform
	 * the rendering system that it needs to redraw a chunk. It does all that work asynchronously,
	 * and we should too.
	 */
	@Inject(at = @At("HEAD"), method = "onLightUpdate")
	private void flywheel$onLightUpdate(LightLayer type, SectionPos pos, CallbackInfo ci) {
		LightUpdater.get(level)
				.onLightUpdate(type, pos.asLong());
	}
}
