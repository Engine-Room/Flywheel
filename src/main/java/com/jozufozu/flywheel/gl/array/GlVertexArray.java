package com.jozufozu.flywheel.gl.array;

import java.util.List;

import org.lwjgl.opengl.GL32;

import com.jozufozu.flywheel.gl.GlObject;
import com.jozufozu.flywheel.gl.GlStateTracker;
import com.mojang.blaze3d.platform.GlStateManager;

public abstract class GlVertexArray extends GlObject {
	protected static final int MAX_ATTRIBS = GL32.glGetInteger(GL32.GL_MAX_VERTEX_ATTRIBS);
	protected static final int MAX_ATTRIB_BINDINGS = 16;

	public static GlVertexArray create() {
		if (GlVertexArrayDSA.SUPPORTED) {
			return new GlVertexArrayDSA();
		} else if (GlVertexArraySeparateAttributes.SUPPORTED) {
			return new GlVertexArraySeparateAttributes();
		} else if (GlVertexArrayGL3.Core33.SUPPORTED) {
			return new GlVertexArrayGL3.Core33();
		} else if (GlVertexArrayGL3.ARB.SUPPORTED) {
			return new GlVertexArrayGL3.ARB();
		} else {
			return new GlVertexArrayGL3.Core();
		}
	}

	public void bindForDraw() {
		GlStateTracker.bindVao(handle());
	}

	public abstract void bindVertexBuffer(int bindingIndex, int vbo, long offset, int stride);

	public abstract void setBindingDivisor(int bindingIndex, int divisor);

	public abstract void bindAttributes(int bindingIndex, int startAttribIndex, List<VertexAttribute> vertexAttributes);

	public abstract void setElementBuffer(int ebo);

	@Override
	protected void deleteInternal(int handle) {
		GlStateManager._glDeleteVertexArrays(handle);
	}
}
