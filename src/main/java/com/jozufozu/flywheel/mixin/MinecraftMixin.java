package com.jozufozu.flywheel.mixin;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.jozufozu.flywheel.api.event.EndClientResourceReloadEvent;
import com.mojang.realmsclient.client.RealmsClient;

import net.minecraft.client.Minecraft;
import net.minecraft.client.main.GameConfig;
import net.minecraft.server.packs.resources.ReloadInstance;
import net.minecraft.server.packs.resources.ReloadableResourceManager;
import net.minecraftforge.fml.ModLoader;

@Mixin(Minecraft.class)
abstract class MinecraftMixin {
	@Shadow
	@Final
	private ReloadableResourceManager resourceManager;

	@Inject(method = "lambda$new$5", at = @At("HEAD"))
	private void flywheel$onEndInitialResourceReload(RealmsClient realmsClient, ReloadInstance reloadInstance, GameConfig gameConfig, Optional<Throwable> error, CallbackInfo ci) {
		ModLoader.get().postEvent(new EndClientResourceReloadEvent((Minecraft) (Object) this, resourceManager, true, error));
	}

	@Inject(method = "lambda$reloadResourcePacks$28", at = @At("HEAD"))
	private void flywheel$onEndManualResourceReload(boolean recovery, CompletableFuture<Void> future, Optional<Throwable> error, CallbackInfo ci) {
		ModLoader.get().postEvent(new EndClientResourceReloadEvent((Minecraft) (Object) this, resourceManager, false, error));
	}
}
