package dev.engine_room.flywheel.impl.mixin;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

import net.neoforged.fml.ModLoader;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Coerce;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.mojang.realmsclient.client.RealmsClient;

import dev.engine_room.flywheel.api.event.EndClientResourceReloadEvent;
import dev.engine_room.flywheel.impl.FlwImpl;
import net.minecraft.client.Minecraft;
import net.minecraft.server.packs.resources.ReloadableResourceManager;

@Mixin(Minecraft.class)
abstract class MinecraftMixin {
	@Shadow
	@Final
	private ReloadableResourceManager resourceManager;

	// Inject at invoke cannot be used in constructors in vanilla Mixin, so use ModifyArg instead.
	@ModifyArg(method = "<init>", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/packs/resources/ReloadableResourceManager;createReload(Ljava/util/concurrent/Executor;Ljava/util/concurrent/Executor;Ljava/util/concurrent/CompletableFuture;Ljava/util/List;)Lnet/minecraft/server/packs/resources/ReloadInstance;"), index = 0)
	private Executor flywheel$onBeginInitialResourceReload(Executor arg0) {
		FlwImpl.freezeRegistries();
		return arg0;
	}

	@Inject(method = "lambda$new$7", at = @At("HEAD"))
	private void flywheel$onEndInitialResourceReload(@Coerce Object minecraft$gameloadcookie, Optional<Throwable> error, CallbackInfo ci) {
		ModLoader.get().postEvent(new EndClientResourceReloadEvent((Minecraft) (Object) this, resourceManager, true, error));
	}

	@Inject(method = "lambda$reloadResourcePacks$39", at = @At("HEAD"))
	private void flywheel$onEndManualResourceReload(boolean bl, @Coerce Object minecraft$gameloadcookie, CompletableFuture<Void> completablefuture, Optional<Throwable> error, CallbackInfo ci) {
		ModLoader.get().postEvent(new EndClientResourceReloadEvent((Minecraft) (Object) this, resourceManager, false, error));
	}
}
