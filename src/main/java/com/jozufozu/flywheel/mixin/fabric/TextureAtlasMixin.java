package com.jozufozu.flywheel.mixin.fabric;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.jozufozu.flywheel.core.StitchedSprite;

import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlas.Preparations;

@Mixin(TextureAtlas.class)
public class TextureAtlasMixin {
	@Inject(method = "reload(Lnet/minecraft/client/renderer/texture/TextureAtlas$Preparations;)V", at = @At("TAIL"))
	private void onTailReload(Preparations preparations, CallbackInfo ci) {
		StitchedSprite.onTextureStitchPost((TextureAtlas) (Object) this);
	}
}
