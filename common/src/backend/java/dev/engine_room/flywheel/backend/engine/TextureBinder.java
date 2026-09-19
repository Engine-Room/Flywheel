package dev.engine_room.flywheel.backend.engine;

import java.util.Collections;

import org.jspecify.annotations.Nullable;
import org.lwjgl.opengl.GL33C;

import com.mojang.blaze3d.opengl.GlConst;
import com.mojang.blaze3d.opengl.GlDevice;
import com.mojang.blaze3d.opengl.GlSampler;
import com.mojang.blaze3d.opengl.GlStateManager;
import com.mojang.blaze3d.opengl.GlTexture;
import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.textures.FilterMode;
import com.mojang.blaze3d.textures.GpuSampler;
import com.mojang.blaze3d.textures.GpuTexture;
import com.mojang.blaze3d.textures.GpuTextureView;

import dev.engine_room.flywheel.backend.Samplers;
import dev.engine_room.flywheel.backend.gl.GlUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.Identifier;

public class TextureBinder {
	public static void bind(int unit, Identifier textureId, @Nullable GpuSampler sampler) {
		GpuTextureView texture = Minecraft.getInstance()
				.getTextureManager()
				.getTexture(textureId)
				.getTextureView();

		bind(unit, texture, sampler);
	}

	// Taken from GlCommandEncoder.trySetup
	public static void bind(int unit, GpuTextureView textureView, @Nullable GpuSampler sampler) {
		GlStateManager._activeTexture(GlConst.GL_TEXTURE0 + unit);
		GlTexture texture = (GlTexture) textureView.texture();
		int i;
		if ((texture.usage() & GpuTexture.USAGE_CUBEMAP_COMPATIBLE) != 0) {
			i = GL33C.GL_TEXTURE_CUBE_MAP;
			GL33C.glBindTexture(i, texture.glId());
		} else {
			i = GlConst.GL_TEXTURE_2D;
			GlStateManager._bindTexture(texture.glId());
		}

		if (sampler != null) {
			GL33C.glBindSampler(unit, ((GlSampler) sampler).getId());
			GlStateManager._texParameter(i, GL33C.GL_TEXTURE_BASE_LEVEL, textureView.baseMipLevel());
			GlStateManager._texParameter(i, GL33C.GL_TEXTURE_MAX_LEVEL, textureView.baseMipLevel() + textureView.mipLevels() - 1);
		}
	}

	public static void bindLightAndOverlay() {
		var gameRenderer = Minecraft.getInstance().gameRenderer;
		GpuSampler sampler = RenderSystem.getSamplerCache().getClampToEdge(FilterMode.LINEAR);
		bind(Samplers.OVERLAY.number, gameRenderer.overlayTexture().getTextureView(), sampler);
		bind(Samplers.LIGHT.number, gameRenderer.lightmap(), sampler);
	}

	public static void bindRenderTarget(RenderTarget target) {
		GlTexture colorTexture = (GlTexture) target.getColorTexture();
		int i = colorTexture.getFbo(
				GlUtil.getGlDevice().directStateAccess(),
				target.getDepthTexture()
		);
		GlStateManager._glBindFramebuffer(GlConst.GL_FRAMEBUFFER, i);
	}
}
