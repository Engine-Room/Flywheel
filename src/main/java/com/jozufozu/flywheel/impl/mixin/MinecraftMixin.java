package com.jozufozu.flywheel.impl.mixin;

import java.util.Optional;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.jozufozu.flywheel.api.event.EndClientResourceReloadEvent;

import net.minecraft.client.Minecraft;
import net.minecraft.server.packs.resources.ReloadableResourceManager;
import net.minecraftforge.fml.ModLoader;

@Mixin(Minecraft.class)
abstract class MinecraftMixin {
	@Shadow
	@Final
	private ReloadableResourceManager resourceManager;

	@Inject(method = "lambda$new$5", at = @At("HEAD"))
	private void flywheel$onEndInitialResourceReload(Minecraft.GameLoadCookie minecraft$gameloadcookie, Throwable error, CallbackInfo ci) {
		ModLoader.get().postEvent(new EndClientResourceReloadEvent((Minecraft) (Object) this, resourceManager, true, Optional.ofNullable(error)));
	}

	@Inject(method = "lambda$reloadResourcePacks$34", at = @At("HEAD"))
	private void flywheel$onEndManualResourceReload(boolean p_168020_, Minecraft.GameLoadCookie p_300647_, Throwable error, CallbackInfo ci) {
		ModLoader.get().postEvent(new EndClientResourceReloadEvent((Minecraft) (Object) this, resourceManager, false, Optional.ofNullable(error)));
	}
}
