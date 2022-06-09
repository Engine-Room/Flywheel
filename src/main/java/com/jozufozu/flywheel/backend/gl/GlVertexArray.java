package com.jozufozu.flywheel.backend.gl;

import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL32;

import com.jozufozu.flywheel.backend.gl.buffer.GlBuffer;
import com.jozufozu.flywheel.backend.gl.buffer.GlBufferType;
import com.jozufozu.flywheel.backend.gl.versioned.GlCompat;
import com.jozufozu.flywheel.core.layout.BufferLayout;
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
	 * The VBO to which each attribute is bound.
	 */
	private final int[] targets = new int[MAX_ATTRIBS];

	/**
	 * Each attribute's data type.
	 */
	private final VertexAttribute[] attributes = new VertexAttribute[MAX_ATTRIBS];

	/**
	 * Each attribute's offset.
	 */
	private final int[] offsets = new int[MAX_ATTRIBS];

	/**
	 * Each attribute's stride.
	 */
	private final int[] strides = new int[MAX_ATTRIBS];


	public GlVertexArray() {
		setHandle(GlStateManager._glGenVertexArrays());
	}

	public void bind() {
		if (!isBound()) {
			GlStateManager._glBindVertexArray(handle());
		}
	}

	public boolean isBound() {
		return handle() == GlStateTracker.getVertexArray();
	}

	public static void unbind() {
		GlStateManager._glBindVertexArray(0);
	}

	public void bindAttributes(GlBuffer buffer, int startIndex, BufferLayout type) {
		bind();

		int targetBuffer = buffer.handle();

		GlBufferType.ARRAY_BUFFER.bind(targetBuffer);

		int i = startIndex;
		int offset = 0;
		final int stride = type.getStride();

		for (VertexAttribute attribute : type.getAttributes()) {
			targets[i] = targetBuffer;
			attributes[i] = attribute;
			offsets[i] = offset;
			strides[i] = stride;

			GL20.glVertexAttribPointer(i++, attribute.size(), attribute.type().getGlEnum(), attribute.normalized(), stride, offset);

			offset += attribute.getByteWidth();
		}
	}

	public void enableArrays(int count) {
		bind();

		for (int i = 0; i < count; i++) {
			enable(i);
		}
	}

	public void disableArrays(int count) {
		bind();

		for (int i = 0; i < count; i++) {
			disable(i);
		}
	}

	private void enable(int i) {
		if (!enabled[i]) {
			GL20.glEnableVertexAttribArray(i);
			enabled[i] = true;
		}
	}

	private void disable(int i) {
		if (enabled[i]) {
			GL20.glDisableVertexAttribArray(i);
			enabled[i] = false;
		}
	}

	protected void deleteInternal(int handle) {
		GlStateManager._glDeleteVertexArrays(handle);
	}

	public void setAttributeDivisor(int index, int divisor) {
		if (divisors[index] != divisor) {
			bind();
			GlCompat.getInstance().instancedArrays.vertexAttribDivisor(index, divisor);
			divisors[index] = divisor;
		}
	}
}
