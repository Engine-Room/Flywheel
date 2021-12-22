package com.jozufozu.flywheel.mixin.atlas;

import java.util.stream.Stream;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.jozufozu.flywheel.core.crumbling.AtlasInfo;

import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.profiling.ProfilerFiller;

@Mixin(TextureAtlas.class)
public abstract class AtlasDataMixin {

	@Shadow
	public abstract ResourceLocation location();

	@Inject(method = "prepareToStitch", at = @At("RETURN"))
	public void stealAtlasData(ResourceManager resourceManager, Stream<ResourceLocation> locationStream, ProfilerFiller profiler, int mipMapLevels, CallbackInfoReturnable<TextureAtlas.Preparations> cir) {
		AtlasInfo._setAtlasData(location(), (SheetDataAccessor) cir.getReturnValue());
	}
}
