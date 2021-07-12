package com.jozufozu.flywheel.mixin.atlas;

import com.jozufozu.flywheel.core.atlas.AtlasInfo;

import net.minecraft.client.renderer.texture.AtlasTexture;

import net.minecraft.profiler.IProfiler;
import net.minecraft.resources.IResourceManager;
import net.minecraft.util.ResourceLocation;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.stream.Stream;

@Mixin(AtlasTexture.class)
public abstract class AtlasDataMixin {

	@Shadow
	public abstract ResourceLocation getId();

	@Inject(method = "stitch", at = @At("RETURN"))
	public void stealAtlasData(IResourceManager resourceManager, Stream<ResourceLocation> locationStream, IProfiler profiler, int mipMapLevels, CallbackInfoReturnable<AtlasTexture.SheetData> cir) {
		AtlasTexture.SheetData value = cir.getReturnValue();

		SheetDataAccessor dataAccessor = (SheetDataAccessor) value;

		AtlasInfo.setAtlasData(getId(), dataAccessor);
	}
}
