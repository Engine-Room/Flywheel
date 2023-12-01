package com.jozufozu.flywheel.backend;

import com.jozufozu.flywheel.api.material.Material;
import com.jozufozu.flywheel.api.material.Transparency;
import com.jozufozu.flywheel.gl.GlTextureUnit;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.AbstractTexture;

public class MaterialUtil {
	public static void setup(Material material) {
		GlTextureUnit.T0.makeActive();
		AbstractTexture texture = Minecraft.getInstance()
				.getTextureManager()
				.getTexture(material.baseTexture());
		texture.setFilter(material.blur(), material.mip());
		RenderSystem.setShaderTexture(0, texture.getId());

		if (!material.backfaceCull()) {
			RenderSystem.disableCull();
		}

		if (material.transparency() != Transparency.OPAQUE) {
			RenderSystem.enableBlend();
			RenderSystem.blendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
		}
	}

	public static void clear(Material material) {
		GlTextureUnit.T0.makeActive();
		RenderSystem.setShaderTexture(0, 0);

		if (!material.backfaceCull()) {
			RenderSystem.enableCull();
		}

		if (material.transparency() != Transparency.OPAQUE) {
			RenderSystem.disableBlend();
			RenderSystem.defaultBlendFunc();
		}
	}

	public static final int DIFFUSE_MASK = 1;
	public static final int LIGHTING_MASK = 1 << 1;
	public static final int BLUR_MASK = 1 << 2;
	public static final int BACKFACE_CULL_MASK = 1 << 3;
	public static final int POLYGON_OFFSET_MASK = 1 << 4;
	public static final int MIP_MASK = 1 << 5;
	public static final int FOG_MASK = 0b11000000;
	public static final int TRANSPARENCY_MASK = 0b11100000000;
	public static final int CUTOUT_MASK = 0b1100000000000;
	public static final int WRITE_MASK_MASK = 0b110000000000000;

	public static int packProperties(Material material) {
		int out = 0;

		if (material.diffuse()) out |= DIFFUSE_MASK;
		if (material.lighting()) out |= LIGHTING_MASK;
		if (material.blur()) out |= BLUR_MASK;
		if (material.backfaceCull()) out |= BACKFACE_CULL_MASK;
		if (material.polygonOffset()) out |= POLYGON_OFFSET_MASK;
		if (material.mip()) out |= MIP_MASK;

		out |= (material.fog()
				.ordinal() & 0x3) << 6;
		out |= (material.transparency()
				.ordinal() & 0x7) << 8;
		out |= (material.cutout()
				.ordinal() & 0x3) << 11;
		out |= (material.writeMask()
				.ordinal() & 0x3) << 13;

		return out;
	}
}
