package com.jozufozu.flywheel.gl.array;

import org.lwjgl.opengl.ARBInstancedArrays;
import org.lwjgl.opengl.GL20C;
import org.lwjgl.opengl.GL30C;
import org.lwjgl.opengl.GL33C;
import org.lwjgl.opengl.GL45C;
import org.lwjgl.system.Checks;

import com.jozufozu.flywheel.gl.GlCompat;
import com.jozufozu.flywheel.gl.GlStateTracker;
import com.jozufozu.flywheel.gl.buffer.GlBufferType;

public interface VertexArray {
	int create();

	void setElementBuffer(int vao, int elementBuffer);

	void enableAttrib(int vao, int index);

	void setupAttrib(int vao, int index, int vbo, int stride, long offset, VertexAttribute attribute);

	void setAttribDivisor(int vao, int attrib, int divisor);

	abstract class GL3 implements VertexArray {
		@Override
		public int create() {
			return GL30C.glGenVertexArrays();
		}

		@Override
		public void setElementBuffer(int vao, int elementBuffer) {
			GlStateTracker.bindVao(vao);
			GlBufferType.ELEMENT_ARRAY_BUFFER.bind(elementBuffer);
		}

		@Override
		public void enableAttrib(int vao, int index) {
			GlStateTracker.bindVao(vao);
			GL20C.glEnableVertexAttribArray(index);
		}

		@Override
		public void setupAttrib(int vao, int index, int vbo, int stride, long offset, VertexAttribute attribute) {
			GlStateTracker.bindVao(vao);
			GlBufferType.ARRAY_BUFFER.bind(vbo);
			attribute.setup(offset, index, stride);
		}
	}

	class InstancedArraysARB extends GL3 {
		@Override
		public void setAttribDivisor(int vao, int attrib, int divisor) {
			GlStateTracker.bindVao(vao);
			ARBInstancedArrays.glVertexAttribDivisorARB(attrib, divisor);
		}

		public VertexArray fallback() {
			if (Checks.checkFunctions(GlCompat.CAPABILITIES.glVertexAttribDivisorARB)) {
				return this;
			}
			// null signals that we don't support instancing.
			return null;
		}
	}

	class InstancedArraysCore extends GL3 {
		@Override
		public void setAttribDivisor(int vao, int attrib, int divisor) {
			GlStateTracker.bindVao(vao);
			GL33C.glVertexAttribDivisor(attrib, divisor);
		}

		public VertexArray fallback() {
			// We know vertex arrays are supported because minecraft required GL32.
			if (Checks.checkFunctions(GlCompat.CAPABILITIES.glVertexAttribDivisor)) {
				return this;
			}
			return new InstancedArraysARB().fallback();
		}

	}

	class DSA implements VertexArray {
		@Override
		public int create() {
			return GL45C.glCreateVertexArrays();
		}

		@Override
		public void setElementBuffer(int vao, int elementBuffer) {
			GL45C.glVertexArrayElementBuffer(vao, elementBuffer);
		}

		@Override
		public void enableAttrib(int vao, int index) {
			GL45C.glEnableVertexArrayAttrib(vao, index);
		}

		@Override
		public void setupAttrib(int vao, int index, int vbo, int stride, long offset, VertexAttribute attribute) {
			GL45C.glVertexArrayVertexBuffer(vao, index, vbo, offset, stride);
			attribute.setupDSA(vao, index);
		}

		@Override
		public void setAttribDivisor(int vao, int attrib, int divisor) {
			GL45C.glVertexArrayBindingDivisor(vao, attrib, divisor);
		}

		public VertexArray fallback() {
			var c = GlCompat.CAPABILITIES;
			if (Checks.checkFunctions(c.glCreateVertexArrays, c.glVertexArrayElementBuffer, c.glEnableVertexArrayAttrib, c.glVertexArrayVertexBuffer, c.glVertexArrayAttribFormat, c.glVertexArrayAttribIFormat)) {

				if (Checks.checkFunctions(c.glVertexArrayBindingDivisor)) {
					return this;
				} else if (Checks.checkFunctions(c.glVertexArrayVertexAttribDivisorEXT)) {
					// Seems like this may happen when a driver supports
					// ARB_direct_state_access but not core instanced arrays?
					return new InstancedArraysEXTDSA();
				}
			}
			return new InstancedArraysCore().fallback();
		}
	}

	class InstancedArraysEXTDSA extends DSA {
		@Override
		public void setAttribDivisor(int vao, int attrib, int divisor) {
			ARBInstancedArrays.glVertexArrayVertexAttribDivisorEXT(vao, attrib, divisor);
		}
	}
}
