package com.jozufozu.flywheel.backend;

import java.util.Comparator;

import com.jozufozu.flywheel.api.material.Material;
import com.jozufozu.flywheel.api.material.Transparency;
import com.jozufozu.flywheel.api.material.WriteMask;
import com.jozufozu.flywheel.gl.GlTextureUnit;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.AbstractTexture;

public class MaterialUtil {
	public static final Comparator<Material> BY_STATE = Comparator.comparing(Material::baseTexture)
			.thenComparing(Material::mip)
			.thenComparing(Material::blur)
			.thenComparing(Material::backfaceCull)
			.thenComparing(Material::polygonOffset)
			.thenComparing(Material::writeMask);

	public static void setup(Material material) {
		setupTexture(material);

		setupBackfaceCull(material.backfaceCull());
		setupTransparency(material.transparency());
		setupWriteMask(material.writeMask());
		setupPolygonOffset(material.polygonOffset());
	}

	private static void setupPolygonOffset(boolean polygonOffset) {
		if (polygonOffset) {
			RenderSystem.polygonOffset(-1.0F, -10.0F);
			RenderSystem.enablePolygonOffset();
		}
	}

	private static void setupWriteMask(WriteMask mask) {
		RenderSystem.depthMask(mask.depth());
		boolean writeColor = mask.color();
		RenderSystem.colorMask(writeColor, writeColor, writeColor, writeColor);
	}

	private static void setupTransparency(Transparency transparency) {
		if (transparency != Transparency.OPAQUE) {
			RenderSystem.enableBlend();
		}

		switch (transparency) {
		case ADDITIVE -> RenderSystem.blendFunc(GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ONE);
		case LIGHTING -> RenderSystem.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE);
		case GLINT ->
				RenderSystem.blendFuncSeparate(GlStateManager.SourceFactor.SRC_COLOR, GlStateManager.DestFactor.ONE, GlStateManager.SourceFactor.ZERO, GlStateManager.DestFactor.ONE);
		case CRUMBLING ->
				RenderSystem.blendFuncSeparate(GlStateManager.SourceFactor.DST_COLOR, GlStateManager.DestFactor.SRC_COLOR, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
		case TRANSLUCENT ->
				RenderSystem.blendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
		}
	}

	private static void setupBackfaceCull(boolean backfaceCull) {
		if (!backfaceCull) {
			RenderSystem.disableCull();
		}
	}

	private static void setupTexture(Material material) {
		GlTextureUnit.T0.makeActive();
		AbstractTexture texture = Minecraft.getInstance()
				.getTextureManager()
				.getTexture(material.baseTexture());
		var textureId = texture.getId();
		texture.setFilter(material.blur(), material.mip());
		RenderSystem.setShaderTexture(0, textureId);
		RenderSystem.bindTexture(textureId);
	}

	public static void reset() {
		resetDiffuse();
		resetBackfaceCull();
		resetTransparency();
		resetWriteMask();
		resetPolygonOffset();
	}

	private static void resetPolygonOffset() {
		RenderSystem.polygonOffset(0.0F, 0.0F);
		RenderSystem.disablePolygonOffset();
	}

	private static void resetWriteMask() {
		RenderSystem.depthMask(true);
		RenderSystem.colorMask(true, true, true, true);
	}

	private static void resetTransparency() {
		RenderSystem.disableBlend();
		RenderSystem.defaultBlendFunc();
	}

	private static void resetBackfaceCull() {
		RenderSystem.enableCull();
	}

	private static void resetDiffuse() {
		GlTextureUnit.T0.makeActive();
		RenderSystem.setShaderTexture(0, 0);
	}

	public static final int DIFFUSE_MASK = 1;
	public static final int LIGHTING_MASK = 1 << 1;
	public static final int BLUR_MASK = 1 << 2;
	public static final int BACKFACE_CULL_MASK = 1 << 3;
	public static final int POLYGON_OFFSET_MASK = 1 << 4;
	public static final int MIP_MASK = 1 << 5;

	public static int packProperties(Material material) {
		int out = 0;

		if (material.diffuse()) out |= DIFFUSE_MASK;
		if (material.lighting()) out |= LIGHTING_MASK;
		if (material.blur()) out |= BLUR_MASK;
		if (material.backfaceCull()) out |= BACKFACE_CULL_MASK;
		if (material.polygonOffset()) out |= POLYGON_OFFSET_MASK;
		if (material.mip()) out |= MIP_MASK;

		out |= (material.writeMask()
				.ordinal() & 0x3) << 6;

		out |= (material.transparency()
				.ordinal() & 0x7) << 8;

		return out;
	}

	public static int packFogAndCutout(Material material) {
		var fog = ShaderIndices.fog()
				.index(material.fog()
						.source());
		var cutout = ShaderIndices.cutout()
				.index(material.cutout()
						.source());

		return fog & 0xFFFF | (cutout & 0xFFFF) << 16;
	}
}
