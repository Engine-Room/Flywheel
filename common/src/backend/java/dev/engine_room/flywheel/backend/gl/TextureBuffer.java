package dev.engine_room.flywheel.backend.gl;

import org.lwjgl.opengl.GL33C;

import com.mojang.blaze3d.opengl.GlStateManager;

public class TextureBuffer extends GlObject {
	public static final int MAX_TEXELS = GlStateManager._getInteger(GL33C.GL_MAX_TEXTURE_BUFFER_SIZE);
	public static final int MAX_BYTES = MAX_TEXELS * 16; // 4 channels * 4 bytes
	private final int format;

	public TextureBuffer() {
		this(GL33C.GL_RGBA32UI);
	}

	public TextureBuffer(int format) {
		handle(GL33C.glGenTextures());
		this.format = format;
	}

	public void bind(int buffer) {
		GL33C.glBindTexture(GL33C.GL_TEXTURE_BUFFER, handle());
		GL33C.glTexBuffer(GL33C.GL_TEXTURE_BUFFER, format, buffer);
	}

	@Override
	protected void deleteInternal(int handle) {
		GL33C.glDeleteTextures(handle);
	}
}
