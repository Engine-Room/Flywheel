package com.jozufozu.flywheel.gl.versioned;

import org.lwjgl.opengl.ARBInstancedArrays;
import org.lwjgl.opengl.GL20C;
import org.lwjgl.opengl.GL30C;
import org.lwjgl.opengl.GL33C;
import org.lwjgl.opengl.GL45C;
import org.lwjgl.opengl.GLCapabilities;
import org.lwjgl.system.Checks;

import com.jozufozu.flywheel.gl.GlStateTracker;
import com.jozufozu.flywheel.gl.array.VertexAttribute;
import com.jozufozu.flywheel.gl.buffer.GlBufferType;
import com.mojang.blaze3d.platform.GlStateManager;

public interface VertexArray {
	int create();

	void setElementBuffer(int vao, int elementBuffer);

	void setupAttrib(int vao, int index, int vbo, int stride, long offset, VertexAttribute attribute);

	void setAttribDivisor(int vao, int attrib, int divisor);

	abstract class GL3 implements VertexArray {
		@Override
		public int create() {
			return GL30C.glGenVertexArrays();
		}

		@Override
		public void setElementBuffer(int vao, int elementBuffer) {
			if (vao != GlStateTracker.getVertexArray()) {
				GlStateManager._glBindVertexArray(vao);
			}
			GlBufferType.ELEMENT_ARRAY_BUFFER.bind(elementBuffer);
		}

		@Override
		public void setupAttrib(int vao, int index, int vbo, int stride, long offset, VertexAttribute attribute) {
			if (vao != GlStateTracker.getVertexArray()) {
				GlStateManager._glBindVertexArray(vao);
			}
			GlBufferType.ARRAY_BUFFER.bind(vbo);

			GL20C.glEnableVertexAttribArray(index);
			attribute.setup(offset, index, stride);
		}

	}

	class InstancedArraysARB extends GL3 {
		public static InstancedArraysARB INSTANCE = new InstancedArraysARB();

		@Override
		public void setAttribDivisor(int vao, int attrib, int divisor) {
			if (vao != GlStateTracker.getVertexArray()) {
				GlStateManager._glBindVertexArray(vao);
			}
			ARBInstancedArrays.glVertexAttribDivisorARB(attrib, divisor);
		}

		public VertexArray fallback(GLCapabilities caps) {
			return isSupported(caps) ? this : null;
		}

		private boolean isSupported(GLCapabilities caps) {
			return Checks.checkFunctions(caps.glVertexAttribDivisorARB);
		}
	}

	class InstancedArraysCore extends GL3 {
		public static InstancedArraysCore INSTANCE = new InstancedArraysCore();

		@Override
		public void setAttribDivisor(int vao, int attrib, int divisor) {
			if (vao != GlStateTracker.getVertexArray()) {
				GlStateManager._glBindVertexArray(vao);
			}
			GL33C.glVertexAttribDivisor(attrib, divisor);
		}

		public VertexArray fallback(GLCapabilities caps) {
			return isSupported(caps) ? this : InstancedArraysARB.INSTANCE.fallback(caps);
		}

		private static boolean isSupported(GLCapabilities caps) {
			// We know vertex arrays are supported because minecraft required GL32.
			return Checks.checkFunctions(caps.glVertexAttribDivisor);
		}
	}

	class DSA implements VertexArray {
		public static final DSA INSTANCE = new DSA();

		@Override
		public int create() {
			return GL45C.glCreateVertexArrays();
		}

		@Override
		public void setElementBuffer(int vao, int elementBuffer) {
			GL45C.glVertexArrayElementBuffer(vao, elementBuffer);
		}

		@Override
		public void setupAttrib(int vao, int index, int vbo, int stride, long offset, VertexAttribute attribute) {
			GL45C.glEnableVertexArrayAttrib(vao, index);
			GL45C.glVertexArrayVertexBuffer(vao, index, vbo, offset, stride);
			attribute.setupDSA(vao, index);
		}

		@Override
		public void setAttribDivisor(int vao, int attrib, int divisor) {
			GL45C.glVertexArrayBindingDivisor(vao, attrib, divisor);
		}

		public VertexArray fallback(GLCapabilities caps) {
			return isSupported(caps) ? this : InstancedArraysCore.INSTANCE.fallback(caps);
		}

		private static boolean isSupported(GLCapabilities caps) {
			return Checks.checkFunctions(caps.glCreateVertexArrays, caps.glVertexArrayElementBuffer, caps.glEnableVertexArrayAttrib, caps.glVertexArrayVertexBuffer, caps.glVertexArrayBindingDivisor, caps.glVertexArrayAttribFormat, caps.glVertexArrayAttribIFormat);
		}
	}
}
