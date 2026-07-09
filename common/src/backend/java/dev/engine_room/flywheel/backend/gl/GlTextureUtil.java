package dev.engine_room.flywheel.backend.gl;

import org.lwjgl.opengl.GL11;

import com.mojang.blaze3d.opengl.GlTexture;
import com.mojang.blaze3d.opengl.GlTextureView;
import com.mojang.blaze3d.textures.GpuTexture;
import com.mojang.blaze3d.textures.GpuTextureView;

public final class GlTextureUtil {
	private GlTextureUtil() {
	}

	public static int glId(GpuTexture texture) {
		if (texture instanceof GlTexture glTexture) {
			return glTexture.glId();
		}
		throw new IllegalStateException("Flywheel's OpenGL backend requires OpenGL textures.");
	}

	public static int glId(GpuTextureView textureView) {
		if (textureView instanceof GlTextureView glTextureView) {
			return glTextureView.glId();
		}
		return glId(textureView.texture());
	}

	public static void bindTexture2D(GpuTextureView textureView) {
		GL11.glBindTexture(GL11.GL_TEXTURE_2D, glId(textureView));
	}

	public static void configureFiltering(boolean blur, boolean mipmap) {
		int minFilter;
		if (blur) {
			minFilter = mipmap ? GL11.GL_LINEAR_MIPMAP_LINEAR : GL11.GL_LINEAR;
		} else {
			minFilter = mipmap ? GL11.GL_NEAREST_MIPMAP_NEAREST : GL11.GL_NEAREST;
		}

		GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, minFilter);
		GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, blur ? GL11.GL_LINEAR : GL11.GL_NEAREST);
	}
}
