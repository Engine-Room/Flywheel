package dev.engine_room.flywheel.backend.engine;

import com.mojang.blaze3d.opengl.GlDevice;
import com.mojang.blaze3d.pipeline.RenderTarget;

import org.lwjgl.opengl.GL32;
import org.lwjgl.opengl.GL33C;

import com.mojang.blaze3d.opengl.GlConst;
import com.mojang.blaze3d.opengl.GlSampler;
import com.mojang.blaze3d.opengl.GlStateManager;
import com.mojang.blaze3d.opengl.GlTexture;
import com.mojang.blaze3d.opengl.GlTextureView;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.textures.FilterMode;
import com.mojang.blaze3d.textures.GpuTexture;

import dev.engine_room.flywheel.backend.Samplers;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.Identifier;

public class TextureBinder {
	// TODO 1.21.11
	public static void bind(Identifier id) {
		GlStateManager._bindTexture(byName(id));
	}

	// Taken from GlCommandEncoder.trySetup
	public static void bind(int unit, GlTextureView textureView, GlSampler sampler) {
		GlStateManager._activeTexture(GlConst.GL_TEXTURE0 + unit);
		GlTexture texture = textureView.texture();
		int i;
		if ((texture.usage() & GpuTexture.USAGE_CUBEMAP_COMPATIBLE) != 0) {
			i = GL33C.GL_TEXTURE_CUBE_MAP;
			GL33C.glBindTexture(i, texture.glId());
		} else {
			i = GL33C.GL_TEXTURE_2D;
			GlStateManager._bindTexture(texture.glId());
		}

		GL33C.glBindSampler(unit, sampler.getId());
		GlStateManager._texParameter(i, GL33C.GL_TEXTURE_BASE_LEVEL, textureView.baseMipLevel());
		GlStateManager._texParameter(i, GL33C.GL_TEXTURE_MAX_LEVEL, textureView.baseMipLevel() + textureView.mipLevels() - 1);
	}

	public static void bindLightAndOverlay() {
		var gameRenderer = Minecraft.getInstance().gameRenderer;
		GlSampler sampler = (GlSampler) RenderSystem.getSamplerCache().getClampToEdge(FilterMode.LINEAR);
		bind(Samplers.OVERLAY.number, (GlTextureView) gameRenderer.overlayTexture().getTextureView(), sampler);
		bind(Samplers.LIGHT.number, (GlTextureView) gameRenderer.lightTexture().getTextureView(), sampler);
	}

	public static void bindRenderTarget(RenderTarget target) {
		GlTexture colorTexture = (GlTexture) target.getColorTexture();
		int i = colorTexture.getFbo(
				((GlDevice) RenderSystem.getDevice()).directStateAccess(),
				target.getDepthTexture()
		);
		GL32.glBindFramebuffer(GL33C.GL_FRAMEBUFFER, i);
	}

	/**
	 * Get a built-in texture by its resource id.
	 *
	 * @param texture The texture's resource id.
	 * @return The texture.
	 */
	public static int byName(Identifier texture) {
		return ((GlTexture) Minecraft.getInstance()
				.getTextureManager()
				.getTexture(texture)
				.getTexture())
				.glId();
	}
}
