package com.jozufozu.flywheel.impl.mixin;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.jozufozu.flywheel.impl.visualization.VisualizationManagerImpl;

import net.minecraft.client.multiplayer.ClientChunkCache;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.SectionPos;
import net.minecraft.world.level.LightLayer;

@Mixin(ClientChunkCache.class)
abstract class ClientChunkCacheMixin {
	@Shadow
	@Final
	ClientLevel level;

	@Inject(at = @At("HEAD"), method = "onLightUpdate")
	private void flywheel$onLightUpdate(LightLayer pType, SectionPos pPos, CallbackInfo ci) {
		var manager = VisualizationManagerImpl.get(level);

		if (manager != null) {
			manager.enqueueLightUpdateSection(pPos.asLong());
		}
	}
}
