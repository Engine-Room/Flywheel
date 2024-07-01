package dev.engine_room.flywheel.backend.mixin;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import dev.engine_room.flywheel.backend.engine.embed.LightUpdateHolder;
import net.minecraft.client.multiplayer.ClientChunkCache;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.SectionPos;
import net.minecraft.world.level.LightLayer;

@Mixin(ClientChunkCache.class)
abstract class ClientChunkCacheMixin {
	@Shadow
	@Final
	ClientLevel level;

	@Inject(method = "onLightUpdate", at = @At("HEAD"))
	private void flywheel$backend$onLightUpdate(LightLayer layer, SectionPos pos, CallbackInfo ci) {
		// This is duplicated from code in impl, but I'm not sure that it
		// makes sense to be generically passed to backends.
		LightUpdateHolder.get(level)
				.add(pos.asLong());
	}
}
