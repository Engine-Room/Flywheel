package com.jozufozu.flywheel.backend.gl;

import com.jozufozu.flywheel.util.AttribUtil;
import com.mojang.blaze3d.platform.GlStateManager;

public class GlVertexArray extends GlObject {
	public GlVertexArray() {
		setHandle(GlStateManager._glGenVertexArrays());
	}

	public void bind() {
		GlStateManager._glBindVertexArray(handle());
	}

	public static void unbind() {
		GlStateManager._glBindVertexArray(0);
	}

	protected void deleteInternal(int handle) {
		GlStateManager._glDeleteVertexArrays(handle);
	}

	public void enableArrays(int count) {
		AttribUtil.enableArrays(count);
	}
}
