package com.jozufozu.flywheel.backend.engine;

import java.util.Comparator;

import org.lwjgl.opengl.GL11;

import com.jozufozu.flywheel.api.material.DepthTest;
import com.jozufozu.flywheel.api.material.Material;
import com.jozufozu.flywheel.api.material.Transparency;
import com.jozufozu.flywheel.api.material.WriteMask;
import com.jozufozu.flywheel.backend.Samplers;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.AbstractTexture;

public final class MaterialRenderState {
	public static final Comparator<Material> COMPARATOR = Comparator.comparing(Material::texture)
			.thenComparing(Material::blur)
			.thenComparing(Material::mipmap)
			.thenComparing(Material::backfaceCulling)
			.thenComparing(Material::polygonOffset)
			.thenComparing(Material::depthTest)
			.thenComparing(Material::transparency)
			.thenComparing(Material::writeMask);

	private MaterialRenderState() {
	}

	public static void setup(Material material) {
		setupTexture(material);
		setupBackfaceCulling(material.backfaceCulling());
		setupPolygonOffset(material.polygonOffset());
		setupDepthTest(material.depthTest());
		setupTransparency(material.transparency());
		setupWriteMask(material.writeMask());
	}

	private static void setupTexture(Material material) {
		Samplers.DIFFUSE.makeActive();
		AbstractTexture texture = Minecraft.getInstance()
				.getTextureManager()
				.getTexture(material.texture());
		texture.setFilter(material.blur(), material.mipmap());
		var textureId = texture.getId();
		RenderSystem.setShaderTexture(0, textureId);
		RenderSystem.bindTexture(textureId);
	}

	private static void setupBackfaceCulling(boolean backfaceCulling) {
		if (backfaceCulling) {
			RenderSystem.enableCull();
		} else {
			RenderSystem.disableCull();
		}
	}

	private static void setupPolygonOffset(boolean polygonOffset) {
		if (polygonOffset) {
			RenderSystem.polygonOffset(-1.0F, -10.0F);
			RenderSystem.enablePolygonOffset();
		} else {
			RenderSystem.polygonOffset(0.0F, 0.0F);
			RenderSystem.disablePolygonOffset();
		}
	}

	private static void setupDepthTest(DepthTest depthTest) {
		switch (depthTest) {
		case OFF -> {
			RenderSystem.disableDepthTest();
		}
		case NEVER -> {
			RenderSystem.enableDepthTest();
			RenderSystem.depthFunc(GL11.GL_NEVER);
		}
		case LESS -> {
			RenderSystem.enableDepthTest();
			RenderSystem.depthFunc(GL11.GL_LESS);
		}
		case EQUAL -> {
			RenderSystem.enableDepthTest();
			RenderSystem.depthFunc(GL11.GL_EQUAL);
		}
		case LEQUAL -> {
			RenderSystem.enableDepthTest();
			RenderSystem.depthFunc(GL11.GL_LEQUAL);
		}
		case GREATER -> {
			RenderSystem.enableDepthTest();
			RenderSystem.depthFunc(GL11.GL_GREATER);
		}
		case NOTEQUAL -> {
			RenderSystem.enableDepthTest();
			RenderSystem.depthFunc(GL11.GL_NOTEQUAL);
		}
		case GEQUAL -> {
			RenderSystem.enableDepthTest();
			RenderSystem.depthFunc(GL11.GL_GEQUAL);
		}
		case ALWAYS -> {
			RenderSystem.enableDepthTest();
			RenderSystem.depthFunc(GL11.GL_ALWAYS);
		}
		}
	}

	private static void setupTransparency(Transparency transparency) {
		switch (transparency) {
		case OPAQUE -> {
			RenderSystem.disableBlend();
		}
		case ADDITIVE -> {
			RenderSystem.enableBlend();
			RenderSystem.blendFunc(GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ONE);
		}
		case LIGHTNING -> {
			RenderSystem.enableBlend();
			RenderSystem.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE);
		}
		case GLINT -> {
			RenderSystem.enableBlend();
			RenderSystem.blendFuncSeparate(GlStateManager.SourceFactor.SRC_COLOR, GlStateManager.DestFactor.ONE, GlStateManager.SourceFactor.ZERO, GlStateManager.DestFactor.ONE);
		}
		case CRUMBLING -> {
			RenderSystem.enableBlend();
			RenderSystem.blendFuncSeparate(GlStateManager.SourceFactor.DST_COLOR, GlStateManager.DestFactor.SRC_COLOR, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
		}
		case TRANSLUCENT -> {
			RenderSystem.enableBlend();
			RenderSystem.blendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
		}
		}
	}

	private static void setupWriteMask(WriteMask mask) {
		RenderSystem.depthMask(mask.depth());
		boolean writeColor = mask.color();
		RenderSystem.colorMask(writeColor, writeColor, writeColor, writeColor);
	}

	public static void reset() {
		resetTexture();
		resetBackfaceCulling();
		resetPolygonOffset();
		resetDepthTest();
		resetTransparency();
		resetWriteMask();
	}

	private static void resetTexture() {
		Samplers.DIFFUSE.makeActive();
		RenderSystem.setShaderTexture(0, 0);
	}

	private static void resetBackfaceCulling() {
		RenderSystem.enableCull();
	}

	private static void resetPolygonOffset() {
		RenderSystem.polygonOffset(0.0F, 0.0F);
		RenderSystem.disablePolygonOffset();
	}

	private static void resetDepthTest() {
		RenderSystem.disableDepthTest();
		RenderSystem.depthFunc(GL11.GL_LEQUAL);
	}

	private static void resetTransparency() {
		RenderSystem.disableBlend();
		RenderSystem.defaultBlendFunc();
	}

	private static void resetWriteMask() {
		RenderSystem.depthMask(true);
		RenderSystem.colorMask(true, true, true, true);
	}
}
