package dev.engine_room.flywheel.backend.mixin;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;

import dev.engine_room.flywheel.lib.internal.FontTextureExtension;
import net.minecraft.client.gui.font.FontSet;
import net.minecraft.client.gui.font.FontTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;

@Mixin(FontSet.class)
public abstract class FontSetMixin {
	// Replace serial random with thread-local random
	@Shadow
	@Final
	private static RandomSource RANDOM = RandomSource.createNewThreadLocalInstance();

	@ModifyExpressionValue(method = "stitch", at = @At(value = "NEW", target = "net/minecraft/client/gui/font/FontTexture"))
	private FontTexture flywheel$setNameAfterCreate(FontTexture original, @Local ResourceLocation name) {
		// Forward the name to the FontTexture so we can forward the name to the BakedGlyphs it creates.
		// We need to know that to determine which Material to use when actually setting up instances.
		((FontTextureExtension) original).flywheel$setName(name);
		return original;
	}
}
