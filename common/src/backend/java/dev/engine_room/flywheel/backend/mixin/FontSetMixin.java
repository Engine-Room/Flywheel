package dev.engine_room.flywheel.backend.mixin;

import java.util.List;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.google.common.collect.Lists;
import com.mojang.blaze3d.font.GlyphInfo;
import com.mojang.blaze3d.font.GlyphProvider;
import com.mojang.blaze3d.font.SheetGlyphInfo;

import dev.engine_room.flywheel.backend.font.AsyncFontTexture;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.IntList;
import net.minecraft.client.gui.font.FontSet;
import net.minecraft.client.gui.font.GlyphRenderTypes;
import net.minecraft.client.gui.font.glyphs.BakedGlyph;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;

@Mixin(FontSet.class)
public abstract class FontSetMixin {
	@Shadow
	@Final
	private TextureManager textureManager;

	@Shadow
	private BakedGlyph missingGlyph;

	@Shadow
	@Final
	private ResourceLocation name;

	@Shadow
	@Final
	private Int2ObjectMap<IntList> glyphsByWidth;

	@Shadow
	public abstract BakedGlyph getGlyph(int character);

	@Unique
	private static final RandomSource RANDOM = RandomSource.createNewThreadLocalInstance();

	@Unique
	private List<AsyncFontTexture> flywheel$textures;


	@Inject(method = "<init>", at = @At("TAIL"))
	public void init(TextureManager textureManager, ResourceLocation name, CallbackInfo ci) {
		flywheel$textures = Lists.newArrayList();
	}

	@Inject(method = "reload", at = @At("TAIL"))
	public void reload(List<GlyphProvider> glyphProviders, CallbackInfo ci) {
		flywheel$closeTextures();
	}

	/**
	 * @author Jozufozu
	 * @reason Use thread safe random
	 */
	@Overwrite
	public BakedGlyph getRandomGlyph(GlyphInfo glyph) {
		IntList intList = this.glyphsByWidth.get(Mth.ceil(glyph.getAdvance(false)));
		if (intList != null && !intList.isEmpty()) {
			// Override to use thread safe random
			// FIXME: can we just replace the static field instead?
			return this.getGlyph(intList.getInt(RANDOM.nextInt(intList.size())));
		}
		return this.missingGlyph;
	}

	/**
	 * @author Jozufozu
	 * @reason Use our stitching
	 */
	@Overwrite
	private BakedGlyph stitch(SheetGlyphInfo glyphInfo) {
		for (AsyncFontTexture fontTexture : flywheel$textures) {
			BakedGlyph bakedGlyph = fontTexture.add(glyphInfo);
			if (bakedGlyph == null) continue;

			return bakedGlyph;
		}
		ResourceLocation resourceLocation = this.name.withSuffix("/" + flywheel$textures.size());
		boolean bl = glyphInfo.isColored();
		GlyphRenderTypes glyphRenderTypes = bl ? GlyphRenderTypes.createForColorTexture(resourceLocation) : GlyphRenderTypes.createForIntensityTexture(resourceLocation);

		AsyncFontTexture fontTexture2 = new AsyncFontTexture(resourceLocation, glyphRenderTypes, bl);
		flywheel$textures.add(fontTexture2);
		BakedGlyph bakedGlyph2 = fontTexture2.add(glyphInfo);

		this.textureManager.register(resourceLocation, fontTexture2);

		return bakedGlyph2 == null ? this.missingGlyph : bakedGlyph2;
	}

	@Inject(method = "close", at = @At("TAIL"))
	private void flywheel$close(CallbackInfo ci) {
		flywheel$closeTextures();
	}

	@Unique
	private void flywheel$closeTextures() {
		for (AsyncFontTexture texture : flywheel$textures) {
			texture.close();
		}

		flywheel$textures.clear();
	}

}
