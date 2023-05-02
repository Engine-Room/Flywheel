package com.jozufozu.flywheel.gl.array;

import org.lwjgl.opengl.GL32;

import com.jozufozu.flywheel.api.layout.BufferLayout;
import com.jozufozu.flywheel.gl.GlObject;
import com.jozufozu.flywheel.gl.GlStateTracker;
import com.mojang.blaze3d.platform.GlStateManager;

public class GlVertexArray extends GlObject {
	public static final VertexArray IMPL = new VertexArray.DSA().fallback();
	private static final int MAX_ATTRIBS = GL32.glGetInteger(GL32.GL_MAX_VERTEX_ATTRIBS);

	/**
	 * Whether each attribute is enabled.
	 */
	private final boolean[] enabled = new boolean[MAX_ATTRIBS];
	/**
	 * Each attribute's divisor.
	 */
	private final int[] divisors = new int[MAX_ATTRIBS];
	/**
	 * Each attribute's data type.
	 */
	private final VertexAttribute[] attributes = new VertexAttribute[MAX_ATTRIBS];
	/**
	 * The VBO to which each attribute is bound.
	 */
	private final int[] targets = new int[MAX_ATTRIBS];
	/**
	 * Each attribute's offset.
	 */
	private final long[] offsets = new long[MAX_ATTRIBS];
	/**
	 * Each attribute's stride.
	 */
	private final int[] strides = new int[MAX_ATTRIBS];

	private int elementBufferBinding = 0;

	public GlVertexArray() {
		setHandle(IMPL.create());
	}

	public void bindForDraw() {
		GlStateTracker.bindVao(handle());
	}

	public static void unbind() {
		GlStateManager._glBindVertexArray(0);
	}

	public void bindAttributes(BufferLayout type, final int vbo, final int startAttrib, final long startOffset) {
		final int vao = handle();
		final int stride = type.getStride();

		int index = startAttrib;
		long offset = startOffset;
		for (var attribute : type.attributes()) {
			if (!enabled[index]) {
				IMPL.enableAttrib(vao, index);
				enabled[index] = true;
			}

			if (shouldSetupAttrib(index, vbo, stride, offset, attribute)) {
				IMPL.setupAttrib(vao, index, vbo, stride, offset, attribute);
				targets[index] = vbo;
				attributes[index] = attribute;
				offsets[index] = offset;
				strides[index] = stride;
			}

			index++;
			offset += attribute.getByteWidth();
		}
	}

	private boolean shouldSetupAttrib(int index, int vbo, int stride, long offset, VertexAttribute attribute) {
		return targets[index] != vbo || offsets[index] != offset || strides[index] != stride || !attribute.equals(attributes[index]);
	}

	protected void deleteInternal(int handle) {
		GlStateManager._glDeleteVertexArrays(handle);
	}

	public void setAttributeDivisor(int index, int divisor) {
		if (divisors[index] != divisor) {
			IMPL.setAttribDivisor(handle(), index, divisor);
			divisors[index] = divisor;
		}
	}

	public void setElementBuffer(int ebo) {
		if (elementBufferBinding != ebo) {
			IMPL.setElementBuffer(handle(), ebo);
			elementBufferBinding = ebo;
		}
	}
}
