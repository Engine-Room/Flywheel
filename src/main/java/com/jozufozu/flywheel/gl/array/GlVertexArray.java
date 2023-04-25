package com.jozufozu.flywheel.gl.array;

import java.util.List;

import org.lwjgl.opengl.GL32;

import com.jozufozu.flywheel.api.layout.BufferLayout;
import com.jozufozu.flywheel.gl.GlObject;
import com.jozufozu.flywheel.gl.GlStateTracker;
import com.jozufozu.flywheel.gl.versioned.GlCompat;
import com.mojang.blaze3d.platform.GlStateManager;

@SuppressWarnings("MismatchedReadAndWriteOfArray")
public class GlVertexArray extends GlObject {

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
		setHandle(GlCompat.vertexArray.create());
	}

	public void bindForDraw() {
		if (!isBound()) {
			GlStateManager._glBindVertexArray(handle());
		}
	}

	private boolean isBound() {
		return handle() == GlStateTracker.getVertexArray();
	}

	public static void unbind() {
		GlStateManager._glBindVertexArray(0);
	}

	public void bindAttributes(BufferLayout type, int vbo, int startAttrib, long startOffset) {
		final int stride = type.getStride();

		List<VertexAttribute> vertexAttributes = type.attributes();
		for (int i = 0; i < vertexAttributes.size(); i++) {
			var attribute = vertexAttributes.get(i);
			int index = i + startAttrib;
			targets[index] = vbo;
			attributes[index] = attribute;
			offsets[index] = startOffset;
			strides[index] = stride;

			GlCompat.vertexArray.setupAttrib(handle(), stride, vbo, startOffset, attribute, index);

			startOffset += attribute.getByteWidth();
		}
	}

	protected void deleteInternal(int handle) {
		GlStateManager._glDeleteVertexArrays(handle);
	}

	public void setAttributeDivisor(int index, int divisor) {
		if (divisors[index] != divisor) {
			GlCompat.vertexArray.setAttribDivisor(handle(), index, divisor);
			divisors[index] = divisor;
		}
	}

	public void setElementBuffer(int ebo) {
		if (elementBufferBinding != ebo) {
			GlCompat.vertexArray.setElementBuffer(handle(), ebo);
			elementBufferBinding = ebo;
		}
	}
}
