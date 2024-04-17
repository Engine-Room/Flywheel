package com.jozufozu.flywheel.backend.gl;

import org.lwjgl.opengl.GL32;

public class TextureBuffer extends GlObject {
	public static final int MAX_TEXELS = GL32.glGetInteger(GL32.GL_MAX_TEXTURE_BUFFER_SIZE);
	public static final int MAX_BYTES = MAX_TEXELS * 16; // 4 channels * 4 bytes

	public TextureBuffer() {
		handle(GL32.glGenTextures());
	}

	public void bind(int buffer) {
		GL32.glBindTexture(GL32.GL_TEXTURE_BUFFER, handle());
		GL32.glTexBuffer(GL32.GL_TEXTURE_BUFFER, GL32.GL_RGBA32UI, buffer);
	}

	@Override
	protected void deleteInternal(int handle) {
		GL32.glDeleteTextures(handle);
	}
}
