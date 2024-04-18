package com.jozufozu.flywheel.impl.mixin;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;

import com.jozufozu.flywheel.api.event.EndClientResourceReloadEvent;

import net.minecraftforge.fml.ModLoader;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.client.Minecraft;
import net.minecraft.server.packs.resources.ReloadableResourceManager;

@Mixin(Minecraft.class)
abstract class MinecraftMixin {
	@Shadow
	@Final
	private ReloadableResourceManager resourceManager;

	@Inject(method = "method_24040", at = @At("HEAD"))
	private void flywheel$onEndInitialResourceReload(Optional<Throwable> error, CallbackInfo ci) {
		ModLoader.get().postEvent(new EndClientResourceReloadEvent((Minecraft) (Object) this, resourceManager, true, error));
	}

	@Inject(method = "method_24228", at = @At("HEAD"))
	private void flywheel$onEndManualResourceReload(boolean recovery, CompletableFuture<Void> future, Optional<Throwable> error, CallbackInfo ci) {
		ModLoader.get().postEvent(new EndClientResourceReloadEvent((Minecraft) (Object) this, resourceManager, false, error));
	}
}
