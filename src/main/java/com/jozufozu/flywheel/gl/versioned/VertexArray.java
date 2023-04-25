package com.jozufozu.flywheel.gl.versioned;

import org.lwjgl.opengl.ARBInstancedArrays;
import org.lwjgl.opengl.GL30;
import org.lwjgl.opengl.GL33;
import org.lwjgl.opengl.GL45C;
import org.lwjgl.opengl.GLCapabilities;

import com.jozufozu.flywheel.gl.GlStateTracker;
import com.jozufozu.flywheel.gl.array.VertexAttribute;
import com.jozufozu.flywheel.gl.buffer.GlBufferType;
import com.mojang.blaze3d.platform.GlStateManager;

public enum VertexArray implements GlVersioned {
	DSA {
		@Override
		public boolean supported(GLCapabilities caps) {
			// The static methods from GL45 and ARBDirectStateAccess all point to GL45C.
			return caps.OpenGL45 || caps.GL_ARB_direct_state_access;
		}

		@Override
		public int create() {
			return GL45C.glCreateVertexArrays();
		}

		@Override
		public void setElementBuffer(int vao, int elementBuffer) {
			GL45C.glVertexArrayElementBuffer(vao, elementBuffer);
		}

		@Override
		public void setupAttrib(int vao, int stride, int vbo, long offset, VertexAttribute attribute, int index) {
			GL45C.glEnableVertexArrayAttrib(vao, index);
			GL45C.glVertexArrayVertexBuffer(vao, index, vbo, offset, stride);
			attribute.setupDSA(vao, index);
		}

		@Override
		public void setAttribDivisor(int vao, int attrib, int divisor) {
			GL45C.glVertexArrayBindingDivisor(vao, attrib, divisor);
		}
	},
	GL_33 {
		@Override
		public boolean supported(GLCapabilities caps) {
			return caps.OpenGL33;
		}

		@Override
		public int create() {
			return GL33.glGenVertexArrays();
		}

		@Override
		public void setElementBuffer(int vao, int elementBuffer) {
			if (vao != GlStateTracker.getVertexArray()) {
				GlStateManager._glBindVertexArray(vao);
			}
			GlBufferType.ELEMENT_ARRAY_BUFFER.bind(elementBuffer);
		}

		@Override
		public void setupAttrib(int vao, int stride, int vbo, long offset, VertexAttribute attribute, int index) {
			if (vao != GlStateTracker.getVertexArray()) {
				GlStateManager._glBindVertexArray(vao);
			}
			GlBufferType.ARRAY_BUFFER.bind(vbo);

			GL33.glEnableVertexAttribArray(index);
			attribute.setup(offset, index, stride);
		}

		@Override
		public void setAttribDivisor(int vao, int attrib, int divisor) {
			if (vao != GlStateTracker.getVertexArray()) {
				GlStateManager._glBindVertexArray(vao);
			}
			GL33.glVertexAttribDivisor(attrib, divisor);
		}
	},
	ARB_INSTANCED_ARRAYS {
		@Override
		public boolean supported(GLCapabilities caps) {
			return caps.GL_ARB_instanced_arrays;
		}

		@Override
		public int create() {
			return GL30.glGenVertexArrays();
		}

		@Override
		public void setElementBuffer(int vao, int elementBuffer) {
			if (vao != GlStateTracker.getVertexArray()) {
				GlStateManager._glBindVertexArray(vao);
			}
			GlBufferType.ELEMENT_ARRAY_BUFFER.bind(elementBuffer);
		}

		@Override
		public void setupAttrib(int vao, int stride, int vbo, long offset, VertexAttribute attribute, int index) {
			if (vao != GlStateTracker.getVertexArray()) {
				GlStateManager._glBindVertexArray(vao);
			}
			GlBufferType.ARRAY_BUFFER.bind(vbo);

			GL30.glEnableVertexAttribArray(index);
			attribute.setup(offset, index, stride);
		}

		@Override
		public void setAttribDivisor(int vao, int attrib, int divisor) {
			if (vao != GlStateTracker.getVertexArray()) {
				GlStateManager._glBindVertexArray(vao);
			}
			ARBInstancedArrays.glVertexAttribDivisorARB(attrib, divisor);
		}
	},
	UNSUPPORTED {
		@Override
		public boolean supported(GLCapabilities caps) {
			return true;
		}

		@Override
		public int create() {
			throw new UnsupportedOperationException("Cannot use vertex arrays");
		}

		@Override
		public void setElementBuffer(int vao, int elementBuffer) {
			throw new UnsupportedOperationException("Cannot use vertex arrays");
		}

		@Override
		public void setupAttrib(int vao, int stride, int vbo, long offset, VertexAttribute attribute, int index) {
			throw new UnsupportedOperationException("Cannot use vertex arrays");
		}

		@Override
		public void setAttribDivisor(int vao, int attrib, int divisor) {
			throw new UnsupportedOperationException("Cannot use vertex arrays");
		}
	};

	public abstract int create();

	public abstract void setElementBuffer(int vao, int elementBuffer);

	public abstract void setupAttrib(int vao, int stride, int vbo, long offset, VertexAttribute attribute, int index);

	public abstract void setAttribDivisor(int vao, int attrib, int divisor);
}
