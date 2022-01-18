package com.jozufozu.flywheel.backend.gl;

import org.lwjgl.opengl.GL20;

import com.jozufozu.flywheel.core.layout.BufferLayout;
import com.jozufozu.flywheel.core.layout.LayoutItem;
import com.mojang.blaze3d.platform.GlStateManager;

public class GlVertexArray extends GlObject {
	public GlVertexArray() {
		setHandle(GlStateManager._glGenVertexArrays());
	}

	public static void bind(int vao) {
		GlStateManager._glBindVertexArray(vao);
	}

	public void bind() {
		bind(handle());
	}

	public static void unbind() {
		GlStateManager._glBindVertexArray(0);
	}

	public void enableArrays(int count) {
		for (int i = 0; i < count; i++) {
			GL20.glEnableVertexAttribArray(i);
		}
	}

	public void disableArrays(int count) {
		for (int i = 0; i < count; i++) {
			GL20.glDisableVertexAttribArray(i);
		}
	}

	public void bindAttributes(int startIndex, BufferLayout type) {
		int offset = 0;
		for (LayoutItem spec : type.getLayoutItems()) {
			spec.vertexAttribPointer(type.getStride(), startIndex, offset);
			startIndex += spec.attributeCount();
			offset += spec.size();
		}
	}

	protected void deleteInternal(int handle) {
		GlStateManager._glDeleteVertexArrays(handle);
	}
}
